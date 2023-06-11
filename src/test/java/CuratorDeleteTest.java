import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuratorDeleteTest {
    private CuratorFramework client;

    @Before
    public void testConnect() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);
        client = CuratorFrameworkFactory.builder().connectString("192.168.1.28:2181")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy).namespace("cynic").build();
        client.start();

    }

    @Test
    public void delete() throws Exception {
        //删除节点,若不存在节点则报错
        //client.delete().forPath("/app2");
        //删除节点及其子节点,若不存在节点则报错
        //client.delete().deletingChildrenIfNeeded().forPath("/app4");
        //必须删除成功,为了防止网络抖动，本质为重试。若不存在节点则报错
        //client.delete().guaranteed().forPath("/app1");
        client.delete().guaranteed().inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                System.out.println("deleted");
                System.out.println(curatorEvent);
            }
        }).forPath("/app1");

    }

    @After
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
