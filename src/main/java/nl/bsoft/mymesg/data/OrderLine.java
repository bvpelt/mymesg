package nl.bsoft.mymesg.data;

import java.io.Serializable;

public class OrderLine implements Serializable {
    private static final long serialVersionUID = 42L;

    private int number;
    private int articleCode;

    public OrderLine() {

    }

    public OrderLine(int number, int articleCode) {
        this.number = number;
        this.articleCode = articleCode;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(int articleCode) {
        this.articleCode = articleCode;
    }

    public String toString() {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append("Number: ");
        strBuf.append(number);
        strBuf.append("\n");
        strBuf.append("ArticleCode: ");
        strBuf.append(articleCode);
        strBuf.append("\n");

        return strBuf.toString();
    }
}
