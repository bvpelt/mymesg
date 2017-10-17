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

    @Rule
    public TestName name = new TestName();

    private Random rand = new Random();

    private final int maxMessages = 20;
    private int nWritten = 0;
    private int nRead = 0;

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
        int nWrittenLocal = 0;
        status = prod.createConnection(myJndi);

        if (0 == status) {
            status = prod.setDestination("MyQueue");
        }

        final int maxPriority = 10;
        int priority = getNextInt(maxPriority);
        while ((0 == status) && (nWrittenLocal < maxMessages / 2)) {
            status = prod.sendTextMessage("Mijn priority: " + priority + " test bericht -- " + nWrittenLocal, priority);
            nWrittenLocal++;
            priority = getNextInt(maxPriority);
        }

        setnWritten(nWrittenLocal);
        Assert.assertEquals(0, status);
        log.info("End   test: {} - nWritten: {}", name.getMethodName(), nWrittenLocal);
    }

    @Test
    public void test02Send() {
        log.info("Start test: {}", name.getMethodName());

        Producer prod = new Producer();

        MyJNDI myJndi = new MyJNDI();
        myJndi.createContext();
        log.trace("Created myJNDI");

        int status;
        int nWrittenLocal = 0;
        status = prod.createConnection(myJndi);

        if (0 == status) {
            status = prod.setDestination("MyQueue");
        }

        while ((0 == status) && (nWrittenLocal < maxMessages / 2)) {
            status = prod.sendTextMessage("Mijn test bericht -- " + nWrittenLocal);
            nWrittenLocal++;
        }

        setnWritten(nWrittenLocal + getnWritten());
        Assert.assertEquals(0, status);
        log.info("End   test: {} - nWritten: {}", name.getMethodName(), nWrittenLocal);
    }

    @Test
    public void test03Read() {
        log.info("Start test: {} - nWritten: {}", name.getMethodName(), nWritten);

        MyJNDI myJndi = new MyJNDI();
        myJndi.createContext();
        log.trace("Created myJNDI");

        int status = 0;

        for (int priority = 0; priority < 10; priority++) {
            Consumer consumer = new Consumer();
            status = consumer.createConnection(myJndi);
            log.info("Try priority: {}", priority);

            if (0 == status) {
                status = consumer.setDestination("MyQueue", priority);
            }

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
            consumer = null;
        }
        Assert.assertEquals(maxMessages, nRead);

        log.info("End   test: {}", name.getMethodName());
    }



    public int getnWritten() {
        return nWritten;
    }

    public void setnWritten(int nWritten) {
        this.nWritten = nWritten;
    }

    public int getnRead() {
        return nRead;
    }

    public void setnRead(int nRead) {
        this.nRead = nRead;
    }

}
