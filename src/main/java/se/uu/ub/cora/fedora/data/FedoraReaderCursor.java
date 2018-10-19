package se.uu.ub.cora.fedora.data;

public class FedoraReaderCursor {
    private final String token;
    private String cursor;

    public FedoraReaderCursor(String token) {
        this.token = token;
    }

    public String getToken() { return token; }

    public String getCursor() { return cursor; }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
