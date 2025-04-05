import java.util.Date;

public class Message {

    public Message (String content, String ipOrigin, String ipDestination) {
        this.content = content;
        this.sendDate = new Date();
        this.ipOrigin = ipOrigin;
        this.ipDestination = ipDestination;
    }

    private String content;
    private Date sendDate;
    private String ipOrigin;
    private String ipDestination;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getIpOrigin() {
        return ipOrigin;
    }

    public void setIpOrigin(String ipOrigin) {
        this.ipOrigin = ipOrigin;
    }

    public String getIpDestination() {
        return ipDestination;
    }

    public void setIpDestination(String ipDestination) {
        this.ipDestination = ipDestination;
    }
}
