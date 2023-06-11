import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CuratorGetSetTest {
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

    /*
     * 查询数据 get getData().forPath
     * 查询子节点 ls getChildren().forPath
     * 查询节点状态 ls -s getData().storingStatIn(stat)
     * */
    @Test
    public void getData() throws Exception {
        Stat stat = new Stat();
        //获取最新数据
        client.sync();
        byte[] data = client.getData().forPath("/app1");
        System.out.println(new String(data));

        //namespace下的子节点为app2,app1,app4
        List<String> children = client.getChildren().forPath("/");
        for (String node : children) {
            System.out.println(node);
        }
        client.getData().storingStatIn(stat).forPath("/");
        System.out.println(stat);

    }

    /*
     * 修改数据  setData()
     * */
    @Test
    public void setData() throws Exception {
        client.setData().forPath("/app2", "grape".getBytes());
    }

    /*
     * 修改数据  setData().withVersion(version)
     * 带版本号修改，保证原子性
     * */
    @Test
    public void setDataWithVersion() throws Exception {
        Stat stat = new Stat();
        client.getData().storingStatIn(stat).forPath("/app2");
        int version = stat.getVersion();
        client.setData().withVersion(version).forPath("/app2", "lychee".getBytes());

    }

    @After
    public void close() {
        if (client != null) {
            client.close();
        }
    }

}
