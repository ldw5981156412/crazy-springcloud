package com.crazymaker.springcloud.stock.service.impl;

import com.crazymaker.springcloud.common.distribute.rateLimit.RateLimitService;
import com.crazymaker.springcloud.common.exception.BusinessException;
import com.crazymaker.springcloud.common.page.DataAdapter;
import com.crazymaker.springcloud.common.page.PageOut;
import com.crazymaker.springcloud.common.page.PageReq;
import com.crazymaker.springcloud.common.util.Encrypt;
import com.crazymaker.springcloud.seckill.api.dto.SeckillSkuDTO;
import com.crazymaker.springcloud.seckill.dao.SeckillSegmentStockDao;
import com.crazymaker.springcloud.seckill.dao.SeckillSkuDao;
import com.crazymaker.springcloud.seckill.dao.po.SeckillSegmentStockPO;
import com.crazymaker.springcloud.seckill.dao.po.SeckillSkuPO;
import com.crazymaker.springcloud.standard.redis.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static com.crazymaker.springcloud.standard.lock.RedisLockService.SEGMENT_DEFAULT;
import static com.crazymaker.springcloud.standard.lua.ScriptHolder.SECKILL_LUA_SHA_1;
import static com.crazymaker.springcloud.standard.lua.ScriptHolder.getSeckillScript;

/**
 * @author liudawei
 * @date 2023/10/3
 */
@Configuration
@Slf4j
@Service
public class SeckillSkuStockServiceImpl {
    @Resource
    SeckillSkuDao seckillSkuDao;

    @Resource
    SeckillSegmentStockDao seckillSegmentStockDao;

    @Resource(name = "zkRateLimitServiceImpl")
    RateLimitService zkRateLimitServiceImpl;

    @Resource
    RedisRepository redisRepository;

    /**
     * 获取所有的秒杀商品列表
     *
     * @param pageReq 当前页 ，从1 开始,和 页的元素个数
     * @return
     */
    public PageOut<SeckillSkuDTO> findAll(PageReq pageReq) {
        Example<SeckillSkuPO> example = Example.of(new SeckillSkuPO());
        Page<SeckillSkuPO> page = seckillSkuDao.findAll(example, PageRequest.of(pageReq.getJpaPage(), pageReq.getPageSize()));
        return DataAdapter.adapterPage(page, SeckillSkuDTO.class);
    }

    public SeckillSkuDTO detail(Long skuId) {
        Optional<SeckillSkuPO> optional = seckillSkuDao.findById(skuId);
        if (optional.isPresent()) {
            SeckillSkuDTO dto = new SeckillSkuDTO();
            SeckillSkuPO sku = optional.get();
            BeanUtils.copyProperties(sku, dto);
            return dto;
        }
        return null;
    }

    @Transactional
    @CacheEvict(cacheNames = {"seckill"}, key = "'seckillsku:' + #skuId")
    public void delete(Long skuId) {
        seckillSkuDao.deleteById(skuId);
        //删除旧的分段数据
        seckillSegmentStockDao.deleteStockBySku(skuId);
    }

    /**
     * 增加秒杀的商品
     *
     * @param stockCount 库存
     * @param title      标题
     * @param price      商品原价格
     * @param costPrice  价格
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public SeckillSkuDTO addSeckillSku(String title, int stockCount, float price, float costPrice) {
        //获取系统时间
        Date nowTime = new Date();
        SeckillSkuPO po = new SeckillSkuPO();
        po.setCostPrice(BigDecimal.valueOf(costPrice));
        po.setPrice(BigDecimal.valueOf(price));
        po.setTitle(title);
        po.setStockCount(stockCount);
        po.setRawStockCount(stockCount);
        po.setCreateTime(nowTime);
        po.setStartTime(DateUtils.addMonths(nowTime, -1));
        po.setEndTime(DateUtils.addMonths(nowTime, 1));

        seckillSkuDao.saveAndFlush(po);
        //插入新的分段数据
//        initSegmentStockAmount(po.getId(), stockCount, SEGMENT_DEFAULT);
        SeckillSkuDTO dto = new SeckillSkuDTO();
        BeanUtils.copyProperties(po, dto);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public SeckillSkuDTO setNewStock(Long skuId, Integer stock) {
        Optional<SeckillSkuPO> optional = seckillSkuDao.findById(skuId);
        if (optional.isPresent()) {
            SeckillSkuPO po = optional.get();
            po.setStockCount(stock);
            po.setRawStockCount(stock);
            seckillSkuDao.save(po);
            SeckillSkuDTO dto = new SeckillSkuDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }
        return null;
    }
    //初始化分段库存
    public void initSegmentStockAmount(Long skuId, int stockAmount, int segment) {
        int avRawStockCount = stockAmount / segment;
        for (int i = 0; i < segment; i++) {
            SeckillSegmentStockPO po = new SeckillSegmentStockPO();
            po.setSkuId(skuId);
            po.setSegmentIndex(i);
            po.setRawStockCount(avRawStockCount);
            //最后一个
            if (i == segment - 1) {
                po.setRawStockCount(avRawStockCount + stockAmount - avRawStockCount * segment);
            }
            po.setStockCount(po.getRawStockCount());
            seckillSegmentStockDao.save(po);
        }
    }

    /**
     * 秒杀暴露
     *
     * @param skuId 商品id
     * @return 暴露的秒杀商品
     */
//    @CachePut(cacheNames = {"seckill"}, key = "'seckillsku:' + #skuId")
    @Transactional(rollbackFor = Exception.class)
    public SeckillSkuDTO exposeSeckillSku(long skuId) {
        Optional<SeckillSkuPO> optional = seckillSkuDao.findById(skuId);
        if (!optional.isPresent()) {
            //秒杀不存在
            throw BusinessException.builder().errMsg("秒杀不存在").build();
        }
        SeckillSkuPO skuPO = optional.get();

        Date startTime = skuPO.getStartTime();
        Date endTime = skuPO.getEndTime();
        //获取系统时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()) {
            //秒杀不存在
            throw BusinessException.builder().errMsg("秒杀没有开始").build();
        }

        if (nowTime.getTime() > endTime.getTime()) {
            //秒杀已经结束
            throw BusinessException.builder().errMsg("秒杀已经结束").build();
        }
        //转换特定字符串的过程，不可逆的算法
        String exposedKey = Encrypt.getMD5(String.valueOf(skuId));

        skuPO.setExposedKey(exposedKey);
        seckillSkuDao.save(skuPO);
        //删除旧的分段库存数据
        seckillSegmentStockDao.deleteStockBySku(skuId);

        //插入新的分段库存数据
        int stock = skuPO.getRawStockCount();
        initSegmentStockAmount(skuId, stock, SEGMENT_DEFAULT);

        SeckillSkuDTO dto = new SeckillSkuDTO();
        BeanUtils.copyProperties(skuPO, dto);
        dto.setExposedKey(exposedKey);
        dto.setExposed(true);

        //加载秒杀的脚本
        cacheSha1();
        redisRepository.set("seckill:stock:" + exposedKey, skuPO.getRawStockCount().toString());
        redisRepository.set("seckill:sku:" + exposedKey, String.valueOf(skuId));
        return dto;
    }

    /**
     * 获取 redis lua 脚本的 sha1 编码,并缓存到 redis
     */
    public String cacheSha1() {
        //秒杀令牌发放 的 lua 脚本
        String scriptLoaded = redisRepository.loadScript(getSeckillScript());
        log.info("scriptLoaded out ={}", scriptLoaded);
        //缓存 sha1 编码，供秒杀使用
        String sha1 = getSeckillScript().getSha1();
        log.info("秒杀 sha1={}", sha1);
        redisRepository.set(SECKILL_LUA_SHA_1, sha1);
        return sha1;
    }
}
