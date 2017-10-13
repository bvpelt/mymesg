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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Rule
    public TestName name = new TestName();

    int maxMessages = 200;

    @Test
    public void test01Send() {
        log.info("Start test: {}", name.getMethodName());

        Producer prod = new Producer();

        MyJNDI myJndi = new MyJNDI();
        myJndi.createContext();
        log.trace("Created myJNDI");

        int status = 0;
        status = prod.createConnection(myJndi);

        if (0 == status) {
            status = prod.setDestination("MyQueue");
        }

        int nWritten = 0;
        while ((0 == status) && (nWritten < maxMessages)){
            status = prod.sendMessage("Mijn test bericht -- " + nWritten);
            nWritten++;
        }

        Assert.assertEquals(0, status);
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void test02Read() {
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
        while  ((0 == status) && (nRead < maxMessages)) {
            String result = consumer.readMessage();
            if (null == result) {
                status = -1;
            } else {
                log.info("Read message '{}'", result);
                consumer.commit();
                nRead++;
            }
        }
        consumer.closeConnection();
        Assert.assertEquals(0, status);

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void test03WriteRead() {
        log.info("Start test: {}", name.getMethodName());

        AtomicLong nWritten = new AtomicLong();
        AtomicLong nRead = new AtomicLong();

        Callable<Integer> task01 = () -> {
            log.info("Started task1");
            Producer prod = new Producer();

            MyJNDI myJndi = new MyJNDI();
            myJndi.createContext();
            log.trace("Created myJNDI");

            int status = 0;
            status = prod.createConnection(myJndi);

            if (0 == status) {
                status = prod.setDestination("MyQueue");
            }

            int nMessages = maxMessages;
            while ((0 == status) && (nMessages > 0)) {
                String msg = "Mijn test bericht " + nMessages;
                status = prod.sendMessage(msg);
                log.info("Sended message: {}", msg);
                nMessages--;
                nWritten.incrementAndGet();
            }

            log.info("Ready sending messages with status: {}", status);
            return status;
        };


        Callable<Integer> task02 = () -> {
            log.info("Started task2");
            Consumer consumer = new Consumer();

            MyJNDI myJndi = new MyJNDI();
            myJndi.createContext();
            log.trace("Created myJNDI");

            int status = 0;
            status = consumer.createConnection(myJndi);

            if (0 == status) {
                status = consumer.setDestination("MyQueue");
            } else {
                log.error("01- Couldnot create connection");
            }

            if (0 != status) {
                log.error("02- Couldnot set destination");
            }

            Random rand = new Random();


            long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            long interval = endTime - startTime;

            while ((0 == status) && (interval < 15000)) {
                log.info("03- Start reading");
                String result = consumer.readMessage();
                if (null == result) {
                    status = -1;
                } else {
                    log.info("04- Read message: {} ", result);

                    nRead.incrementAndGet();

                    // Wait for a wile
                    long delay = rand.nextLong();
                    if (delay < 0) {
                        delay = -1 * delay;
                    }
                    delay = delay % 5000L;
                    log.info("05- Sleep for {} ms", delay);
                    TimeUnit.MICROSECONDS.sleep(delay);
                    log.info("06- Awakened after {} ms", delay);

                    // commit transaction
                    status = consumer.commit();
                    if (0 != status) {
                        log.error("07- Could not commit");
                    }

                    endTime = System.currentTimeMillis();
                    interval = endTime - startTime;
                    log.info("10- Status: {}, Interval: {}", status, interval);
                }
            }

            return status;
        };


        log.info("Start write executor - 1 thread");
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Integer>> callables = Arrays.asList(task01, task02, task02, task02, task02, task02);

        try {
            executor.invokeAll(callables)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(s -> log.info("Result: {}", s));
            executor.shutdown();
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            log.error("Interruption occured", ie);
        }

        Assert.assertEquals(nWritten.get(), nRead.get());

        log.info("End   test: {}", name.getMethodName());
    }

}
