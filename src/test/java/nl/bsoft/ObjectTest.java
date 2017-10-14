package nl.bsoft;

import nl.bsoft.mymesg.consumer.Consumer;
import nl.bsoft.mymesg.data.Order;
import nl.bsoft.mymesg.data.OrderLine;
import nl.bsoft.mymesg.lookup.MyJNDI;
import nl.bsoft.mymesg.producer.Producer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit test for simple App.
 */
public class ObjectTest {
    private final Logger log = LoggerFactory.getLogger(ObjectTest.class);
    private final int maxMessages = 20;
    @Rule
    public TestName name = new TestName();

    @Test
    public void test04Send() {
        log.info("Start test: {}", name.getMethodName());

        Producer prod = new Producer();

        MyJNDI myJndi = new MyJNDI();
        myJndi.createContext();
        log.trace("Created myJNDI");

        int status;
        status = prod.createConnection(myJndi);

        if (0 == status) {
            status = prod.setDestination("MyQueue");
        }

        int nWritten = 0;
        while ((0 == status) && (nWritten < maxMessages)) {
            Set<OrderLine> orderLines = new HashSet<OrderLine>();
            orderLines.add(new OrderLine(1 + nWritten * 3, 100));
            orderLines.add(new OrderLine(2 + nWritten * 3, 200));
            orderLines.add(new OrderLine(3 + nWritten * 3, 300));

            Order order = new Order("Test orders: " + nWritten, orderLines);

            status = prod.sendMessage(order);
            nWritten++;
        }

        Assert.assertEquals(0, status);
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void test05Read() {
        log.info("Start test: {}", name.getMethodName());

        Consumer consumer = new Consumer();

        MyJNDI myJndi = new MyJNDI();
        myJndi.createContext();
        log.trace("Created myJNDI");

        int status;
        status = consumer.createConnection(myJndi);

        if (0 == status) {
            status = consumer.setDestination("MyQueue");
        }

        int nRead = 0;
        while ((0 == status) && (nRead < maxMessages)) {
            Order order = consumer.readOrderMessage();

            if (null == order) {
                status = -1;
                log.error("No object received");
            } else {
                    log.debug("Read message '{}'", order.toString());
                    consumer.commit();
                    nRead++;
            }
        }
        consumer.closeConnection();
        Assert.assertEquals(0, status);

        log.info("End   test: {}", name.getMethodName());
    }

}
