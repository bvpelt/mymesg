package nl.bsoft;

import nl.bsoft.mymesg.consumer.Consumer;
import nl.bsoft.mymesg.lookup.MyJNDI;
import nl.bsoft.mymesg.producer.Producer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Unit test for simple App.
 */
public class TextTest {
    private final Logger log = LoggerFactory.getLogger(TextTest.class);
    private final int maxMessages = 20;
    private Random rand = new Random();

    @Rule
    public TestName name = new TestName();

    private int getNextInt(final int maxInt) {

        int number = rand.nextInt();

        if (number < 0) {
            number = -1 * number;
        }

        number = number % maxInt;
        return number;
    }

    @Test
    public void test01Send() {
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
        final int maxPriority = 10;
        int priority = getNextInt(maxPriority);
        while ((0 == status) && (nWritten < maxMessages / 2)) {
            status = prod.sendTextMessage("Mijn priority: " + priority + " test bericht -- " + nWritten, priority);
            nWritten++;
            priority = getNextInt(maxPriority);
        }

        Assert.assertEquals(0, status);
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void test02Send() {
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
        while ((0 == status) && (nWritten < maxMessages / 2)) {
            status = prod.sendTextMessage("Mijn test bericht -- " + nWritten);
            nWritten++;
        }

        Assert.assertEquals(0, status);
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void test03Read() {
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
            String result = consumer.readTextMessage();
            if (null == result) {
                status = -1;
            } else {
                log.debug("Read message '{}'", result);
                consumer.commit();
                nRead++;
            }
        }
        consumer.closeConnection();
        Assert.assertEquals(0, status);

        log.info("End   test: {}", name.getMethodName());
    }


}
