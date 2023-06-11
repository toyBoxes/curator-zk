import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

public class TicketSaleTest implements Runnable{
    private  int ticket=10;
    //分布式可重入锁
    private InterProcessMutex lock;
    public  TicketSaleTest(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("192.168.1.28:2181")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy).build();
        client.start();
        lock=new InterProcessMutex(client,"/12306");

    }

    @Override
    public void run() {
        while (true) {
            //加锁
            try {
                lock.acquire(2, TimeUnit.SECONDS);

                if (ticket > 0) {
                    System.out.println(Thread.currentThread() + ":" + ticket);
                    ticket--;
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    lock.release();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            //释放锁
        }
    }
}
