package x.hostname.http.bff;

public class BffResponsePojo {

    private String hostname;
    private String bff;

    public BffResponsePojo(String hostname, String bff) {
        this.hostname = hostname;
        this.bff = bff;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getBff() {
        return bff;
    }

    public void setBff(String bff) {
        this.bff = bff;
    }

}
