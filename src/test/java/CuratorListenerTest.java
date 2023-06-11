import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.Logger;

public class CuratorListenerTest {
    private CuratorFramework client;

    @Before
    public void testConnect() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);
        client = CuratorFrameworkFactory.builder().connectString("192.168.1.28:2181")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy).namespace("cynic").build();
        client.start();
        //若不存在，则创建节点
        if(client.checkExists().forPath("/app1")==null&&client.checkExists().forPath("/app2")==null&&client.checkExists().forPath("/app3")==null){
            client.create().forPath("/app1");
            client.create().forPath("/app2", "purple".getBytes());
            client.create().forPath("/app3", "strawberry".getBytes());
        }
    }
    /*
    * 指定节点注册监听
    * */
    @Test
    public void  nodeCacheTest() throws Exception {
        NodeCache nodeCache=new NodeCache(client,"/app2",false);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                //增加当前节点，修改当前节点,删除当前节点/app2数据后，可实时监听数据的变化
                //增加子节点不会触发监听
                //方式一.查看修改后数据
                System.out.println("当前节点发生变化");
                byte[] data=client.getData().forPath("/app2");
                System.out.println(new String(data));
                //方式二.查看修改后数据
                byte[] curData=nodeCache.getCurrentData().getData();
                System.out.println(new String(curData));
            }
        });
        //若为true ,构建初始化，若存在子节点，将节点及子节点数据保存
        nodeCache.start(true);
        /*while (true){ //测试时使用
        }*/
    }
    @Test
    public void  ChildrenCacheTest() throws Exception {
        PathChildrenCache childrenCache=new PathChildrenCache(client,"/app2",true);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            //监听子节点变化，当前节点变化则不触发监听事件
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                System.out.println("子节点发生变化");
                //System.out.println(event);
                //获取最初数据
                List<ChildData> initial=event.getInitialData();
                //获取修改后的数据
                List<ChildData> children=childrenCache.getCurrentData();

                PathChildrenCacheEvent.Type type=event.getType();
                if(type.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                    byte[] data=event.getData().getData();
                    System.out.println(new String(data));
                }

            }
        });
        childrenCache.start();
        /*while (true){//测试时使用

        }*/
    }
    @Test
    public void  treeCacheTest() throws Exception{
        TreeCache treeCache=new TreeCache(client,"/app2");
        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
                TreeCacheEvent.Type type=event.getType();
                if(type.equals(TreeCacheEvent.Type.NODE_UPDATED)) {
                    System.out.println("当前节点或子节点发生变化");
                    System.out.println(new String(event.getData().getData()));
                }
            }
        });
        treeCache.start();
        /*while (true){//测试时使用

        }*/

    }


    @After
    public void deleteAndClose() throws Exception {
        if (client != null) {
            List<String> children = client.getChildren().forPath("/");
            for (String node : children) {
                client.delete().guaranteed().forPath("/"+node);
            }
            //client.delete().guaranteed().forPath("/"+node)
            client.close();
        }
    }
}
