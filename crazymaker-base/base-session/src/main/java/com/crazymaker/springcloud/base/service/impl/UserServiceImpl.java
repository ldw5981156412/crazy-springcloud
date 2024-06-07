package com.crazymaker.springcloud.base.service.impl;

import com.crazymaker.springcloud.base.dao.UserDao;
import com.crazymaker.springcloud.base.dao.po.UserPO;
import com.crazymaker.springcloud.base.security.utils.AuthUtils;
import com.crazymaker.springcloud.common.dto.UserDTO;
import com.crazymaker.springcloud.common.exception.BusinessException;
import com.crazymaker.springcloud.common.util.JsonUtil;
import com.crazymaker.springcloud.standard.redis.RedisRepository;
import com.crazymaker.springcloud.user.info.api.dto.LoginInfoDTO;
import com.crazymaker.springcloud.user.info.api.dto.LoginOutDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.crazymaker.springcloud.common.context.SessionHolder.G_USER;

@Slf4j
@Service
public class UserServiceImpl {

    @Resource
    private UserDao userDao;

    @Resource
    private PasswordEncoder passwordEncoder;
    //缓存操作服务
    @Resource
    private RedisRepository redisRepository;
    //redis 会话存储服务
    @Resource
    private RedisOperationsSessionRepository sessionRepository;

    public UserDTO getUser(Long id){
        UserPO userPO = userDao.findByUserId(id);
        if (userPO == null) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(userPO, userDTO);
            return userDTO;
        }
        return null;
    }

    /**
     * 登录处理
     * @param dto
     * @return
     */
    public LoginOutDTO login(LoginInfoDTO dto){
        String username = dto.getUsername();
        List<UserPO> list = userDao.findAllByUsername(username);
        if(null == list || list.size() <= 0){
            throw BusinessException.builder().errMsg("用户名或者密码错误").build();
        }
        UserPO userPO = list.get(0);
        String encoded = userPO.getPassword();
        String encode = dto.getPassword();
        boolean matched = passwordEncoder.matches(encode, encoded);
        if(!matched){
            throw BusinessException.builder().errMsg("用户名或者密码错误").build();
        }
        //设置 session，方便SpringSecurity 进行权限验证
        return setSession(userPO);
    }
    /**
     * 1: 将 userid -> session id 作为键值对缓存起来, 防止频繁创建 session
     * 2: 将用户信息保存到分布式 session ，
     * 3：创建 JWT token , 提供给 SpringSecurity 进行权限验证
     *
     * @param userPO 用户信息
     * @return 登录的输出信息
     */
    private LoginOutDTO setSession(UserPO userPO){
        if(null == userPO){
            throw BusinessException.builder().errMsg("用户不存在或者密码错误").build();
        }
        /**
         * 根据用户id查询之前保持的session id
         * 防止频繁登录的时候会话被大量创建
         */
        String uid = String.valueOf(userPO.getUserId());
        String sid = redisRepository.getSessionId(uid);

        Session session = null;

        try {
            session = sessionRepository.findById(sid);
        } catch (Exception e) {
            log.info("查找现有的session 失败，将创建一个新的 session");
        }
        if(null == session){
            session = sessionRepository.createSession();
            //新的 session id，和用户 id一起作为 k-v 键值对进行保存
            //用户访问的时候，可以根据 用户 id 查找 session id
            sid = session.getId();
            redisRepository.setSessionId(uid,sid);
        }
        String salt = userPO.getPassword();
        //构建JWT令牌
        String token = AuthUtils.buildToken(sid, salt);
        /**
         * 将用户信息缓存到分布式会话
         */
        UserDTO cacheDto = new UserDTO();
        BeanUtils.copyProperties(userPO,cacheDto);
        cacheDto.setToken(token);
        session.setAttribute(G_USER, JsonUtil.pojoToJson(cacheDto));
        LoginOutDTO outDTO = new LoginOutDTO();
        BeanUtils.copyProperties(cacheDto,outDTO);
        return outDTO;
    }
}
