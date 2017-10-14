package nl.bsoft.mymesg.producer;

import nl.bsoft.mymesg.lookup.MyJNDI;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;

public class Producer {
    private final Logger log = LoggerFactory.getLogger(Producer.class);

    private Connection connection = null;
    private InitialContext jndi = null;
    private Destination destination = null;
    private MessageProducer producer = null;
    private Session session = null;

    public Producer() {

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
                ActiveMQConnectionFactory conFactory = (ActiveMQConnectionFactory) jndi
                        .lookup("ConnectionFactory");
                log.trace("Found connectionFactory");

                conFactory.setTrustAllPackages(true);

                // Getting JMS connection from the server and starting it
                connection = conFactory.createConnection();
                log.trace("Created connection");

                connection.start();
                log.trace("Started connection");
            } catch (NamingException ne) {
                status = -1;
                log.error("Error finding jndi properties", ne);
            } catch (JMSException je) {
                status = -1;
                log.error("Error creating jms connection", je);
            }
        } else {
            status = 1;
            log.warn("A connection already exists");
        }
        return status;
    }

    /**
     * Set the destination of messages the producer will send
     *
     * @param queueName - the name of the queue
     * @return 0 - Destination set
     * -1 - Destination not set
     */
    public int setDestination(String queueName) {
        int status = 0;

        try {
            destination = (Destination) jndi.lookup(queueName);
            log.trace("Found queue: {}", queueName);

            // JMS messages are sent and received using a Session. We will
            // create here a non-transactional session object. If you want
            // to use transactions you should set the first parameter to 'true'
            session = connection.createSession(true,
                    Session.AUTO_ACKNOWLEDGE);
            log.trace("Created session");

            // MessageProducer is used for sending messages (as opposed
            // to MessageConsumer which is used for receiving them)
            producer = session.createProducer(destination);
        } catch (NamingException ne) {
            status = -1;
            log.error("Problem finding jndi parameter", ne);
        } catch (JMSException je) {
            status = -1;
            log.error("Problem creating session or producer", je);
        }
        return status;
    }

    /**
     * Sending a text message
     *
     * @param message the text message to send
     * @return 0 - message send
     * -1 - message not send
     */
    public int sendTextMessage(String message) {
        int status = 0;

        try {
            // We will send a small text message saying 'Hello World!'
            TextMessage msg = session.createTextMessage(message);
            log.trace("Created text message: {}", message);

            // Here we are sending the message!
            producer.send(msg);
            session.commit();
            log.debug("Sent message '{}'", msg.getText());
        } catch (JMSException je) {
            try {
                session.rollback();
            } catch (JMSException jes) {
                log.error("Problem during rollback", jes);
            }
            status = -1;
            log.error("Problem creating or sending the message", je);
        }
        return status;
    }

    /**
     * Sending a text message
     *
     * @param message the text message to send
     * @return 0 - message send
     * -1 - message not send
     */
    public int sendTextMessage(String message, int priority) {
        int status = 0;

        try {
            // We will send a small text message saying 'Hello World!'
            TextMessage msg = session.createTextMessage(message);
            log.trace("Created text message: {}", message);

            // Here we are sending the message!
            producer.send(msg, DeliveryMode.PERSISTENT, priority,0);
            //producer.send(msg);
            session.commit();
            log.debug("Sent message '{}'", msg.getText());
        } catch (JMSException je) {
            try {
                session.rollback();
            } catch (JMSException jes) {
                log.error("Problem during rollback", jes);
            }
            status = -1;
            log.error("Problem creating or sending the message", je);
        }
        return status;
    }
    /**
     * Sending a object message
     *
     * @param message the object to send
     * @return 0 - message send
     * -1 - message not send
     */
    public int sendMessage(Serializable message) {
        int status = 0;

        try {
            // We will send a small text message saying 'Hello World!'
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject(message);
            log.trace("Created object message: {}", message.toString());

            // Here we are sending the message!
            producer.send(msg);
            session.commit();
            log.debug("Sent message");
        } catch (JMSException je) {
            try {
                session.rollback();
            } catch (JMSException jes) {
                log.error("Problem during rollback", jes);
            }
            status = -1;
            log.error("Problem creating or sending the message", je);
        }
        return status;
    }
}
