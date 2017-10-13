package nl.bsoft.mymesg.consumer;


import nl.bsoft.mymesg.lookup.MyJNDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Consumer {
    private final Logger log = LoggerFactory.getLogger(Consumer.class);

    private InitialContext jndi = null;
    private Connection connection = null;
    private Session session = null;
    private MessageConsumer consumer = null;

    public Consumer() {

    }

    /**
     * Create a jms connection
     *
     * @return 0 - jms connection created
     * 1 - jms connection existed
     * -1 - no jms connection created
     */
    public int createConnection(MyJNDI myJndi) {
        int status = 0;
        if (null == connection) {
            try {
                // Obtain a JNDI connection
                jndi = new InitialContext(myJndi.getProps());

                // Look up a JMS connection factory
                ConnectionFactory connectionFactory = (ConnectionFactory) jndi
                        .lookup("ConnectionFactory");
                log.trace("Found connectionFactory");

                connection = connectionFactory.createConnection();

                connection.start();
            } catch (NamingException ne) {
                status = -1;
                log.error("Problem using jndi", ne);
            } catch (JMSException je) {
                status = -1;
                log.error("Problem using jms", je);
            }
        } else {
            status = 1;
        }
        return status;
    }

    public int setDestination(String queueName) {
        int status = 0;

        try {
            // Creating session for receiving messages
            session = connection.createSession(true,
                    Session.AUTO_ACKNOWLEDGE);

            // Getting the queue
            Destination destination = (Destination) jndi.lookup(queueName);
            log.trace("Found queue: {}", queueName);

            // MessageConsumer is used for receiving (consuming) messages
            consumer = session.createConsumer(destination);
        } catch (NamingException ne) {
            status = -1;
            log.error("Problem using jndi", ne);
        } catch (JMSException je) {
            status = -1;
            log.error("Problem using jms", je);
        }
        return status;
    }

    public String readMessage() {
        String result = null;
        long timeout = 1000; //ms
        try {
            Message message = consumer.receive(timeout);

            // There are many types of Message and TextMessage
            // is just one of them. Producer sent us a TextMessage
            // so we must cast to it to get access to its .getText()
            // method.
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                result = textMessage.getText();
                log.info("Received message '{}'", result);
            }
        } catch (JMSException je) {
            try {
                session.rollback();
            } catch (JMSException jes) {
                log.error("Problem during rollback", jes);
            }
            log.error("Problem using jms", je);
        }
        return result;
    }

    public int commit() {
        int status = 0;

        try {
            session.commit();
        } catch (JMSException je) {
            status = -1;
            log.error("Problem in jms", je);
        }

        return status;
    }

    public int closeConnection() {
        int status = 0;
        try {
            connection.close();
        } catch (JMSException je) {
            status = -1;
            log.error("Problem in jms", je);
        }
        return status;
    }

}
