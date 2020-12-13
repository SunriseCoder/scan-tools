package dto;

import java.util.ArrayList;
import java.util.List;

public class HTTPRequest {
    private String method;
    private boolean ssl;
    private String host;
    private int port;
    private String path;
    private String protocol;

    private List<String> headers;

    public HTTPRequest() {
        this.headers = new ArrayList<>();
    }

    public String getMethod() {
        return method;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPath(String path) {
        this.path = path.isEmpty() ? "/" : path;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void addHeader(String header) {
        this.headers.add(header);
    }

    public byte[] generateHttpRequestAsBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" ").append(protocol).append("\r\n");
        for (String header : headers) {
            sb.append(header);
        }
        sb.append("\r\n");
        return sb.toString().getBytes();
    }

    @Override
    public String toString() {
        return "[method=" + method + ", ssl=" + ssl + ", host=" + host + ", port=" + port + ", path=" + path
                + ", protocol=" + protocol + ", headers=String[" + headers.size() + "]]";
    }
}
