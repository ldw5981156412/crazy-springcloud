package com.crazymaker.springcloud.distribute.zookeeper;

import com.crazymaker.springcloud.standard.context.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @author liudawei
 * @date 2023/9/30
 **/
@Slf4j
public class ZKClient {
    private static ZKClient singleton = null;
    //Zk集群地址
    private String zkAddress = "127.0.0.1:2181";
    private CuratorFramework client;

    public ZKClient() {
    }

    public ZKClient(String zkAddress) {
        this.zkAddress = zkAddress;
        init();
    }

    public static ZKClient getSingleton() {
        if (null == singleton) {
            singleton = (ZKClient) SpringContextUtil.getBean("zKClient");
        }
        return singleton;
    }

    public CuratorFramework getClient() {
        return client;
    }

    public synchronized void init() {
        if (null != client) {
            return;
        }
        new Thread(() -> {
            //创建客户端
            client = ClientFactory.createSimple(zkAddress);
            //启动客户端实例,连接服务器
            client.start();
        }).start();
    }

    public void destroy() {
        CloseableUtils.closeQuietly(client);
    }
    //创建节点
    public void createNode(String zkPath,String data){
        try {
            // 创建一个 ZNode 节点
            // 节点的数据为 payload
            byte[] payload = "to set content".getBytes("UTF-8");
            if (data != null) {
                payload = data.getBytes("UTF-8");
            }
            client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath,payload);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查节点
     * @param zkPath
     * @return
     */
    public boolean isNodeExist(String zkPath){
        try {
            Stat stat = client.checkExists().forPath(zkPath);
            if (stat == null) {
                log.info("节点不存在:", zkPath);
                return false;
            }else{
                log.info("节点存在 stat is:", stat.toString());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除节点
     * @param zkPath
     */
    public void deleteNode(String zkPath) {
        try {
            if (!isNodeExist(zkPath)){
                return;
            }
            client.delete().forPath(zkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建 临时 顺序 节点
     * @param srcPath
     * @return
     */
    public String createEphemeralSeqNode(String srcPath){
        try {
            return client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(srcPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
