import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuratorCreateTest {
    private CuratorFramework client;

    @Before
    public void testConnect() {
        /*
         * 重试策略
         * baseSleepTimeMs n 基础睡眠时间 ,即初始等待时间，第一次等待n毫秒重试，第二次等待2*n重试,以此类推
         * maxRetries 最大重试次数
         * */

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);
        /*
         * String connectString,连接地址，多个用逗号隔开: 192.168.1.28:2181,
         * int sessionTimeoutMs,会话超时时间:客户端与服务器建立连接后，如果长时间未通信，则断开连接，连接后与断开连接之间的时间为会话超时时间
         * int connectionTimeoutMs,连接超时时间:客户端与服务器建立连接超过该时间,则继续重试建立连接
         * RetryPolicy retryPolicy 重试策略:
         * */
        //第一种连接方式
        //CuratorFramework client1=CuratorFrameworkFactory.newClient("192.168.1.28:2181",60*1000,15*1000,retryPolicy);

        //client1.start();
        //第二种连接方式,namespace为添加目录前缀
        client = CuratorFrameworkFactory.builder().connectString("192.168.1.28:2181")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy).namespace("cynic").build();
        client.start();

    }

    @Test
    public void testCreate() throws Exception {

        //创建节点，如果未指定数据，默认将客户端ip作为数据存储
        client.create().forPath("/app1");
    }

    //创建多级节点，并指定数据
    @Test
    public void testCreateWithData() throws Exception {

        client.create().forPath("/app2", "purple".getBytes());

    }

    //创建多级节点，并指定节点类型，默认类型为持久化
    @Test
    public void testCreateType() throws Exception {
        //创建临时化节点
        client.create().withMode(CreateMode.EPHEMERAL).forPath("/app3", "strawberry".getBytes());

    }

    @Test
    public void testCreateParent() throws Exception {
        //创建父级节点
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/app4/p1", "lychee".getBytes());

    }

    @After
    public void testClose() {
        if (client != null) {
            client.close();
        }

    }

}
