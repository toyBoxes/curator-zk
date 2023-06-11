public class LockTest {
    public static void main(String[] args) {
        TicketSaleTest ticketSaleTest=new TicketSaleTest();
        Thread t1=new Thread(ticketSaleTest,"携程");
        Thread t2=new Thread(ticketSaleTest,"飞猪");
        t1.start();
        t2.start();
    }
}
