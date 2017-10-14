package nl.bsoft.mymesg.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Order implements Serializable {
    private static final long serialVersionUID = 43L;

    private String orderName;
    private Set<OrderLine> orderLines;

    public Order() {

    }

    public Order(String orderName) {
        this.orderName = orderName;
    }

    public Order(String orderName, Set<OrderLine> orderLines) {
        this.orderName = orderName;
        this.orderLines = orderLines;
    }

    public void addOrderLine(OrderLine orderLine) {
        if (null == orderLines) {
            orderLines = new HashSet<OrderLine>();
        }

        if (!orderLines.contains(orderLine)) {
            orderLines.add(orderLine);
        }
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Set<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(Set<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    public String toString() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("Order: ");
        strBuf.append(orderName);
        strBuf.append("\n");

        Iterator<OrderLine> i = orderLines.iterator();
        while (i.hasNext()) {
            OrderLine ol = i.next();
            strBuf.append("Orderline: ");
            strBuf.append(ol.toString());
            strBuf.append("\n");
        }

        return strBuf.toString();
    }
}
