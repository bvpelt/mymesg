package nl.bsoft.mymesg.lookup;

import javax.naming.Context;
import java.util.Properties;

public class MyJNDI {

    private Properties props = null;

    public MyJNDI() {
    }

    ;

    public void createContext() {
        props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
        props.setProperty("queue.MyQueue", "MyQueue");
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }
}
