package dto;

import java.util.ArrayList;
import java.util.List;

public class HTTPResponse {
    private List<String> headers;
    private byte[] body;
    private String statusLine;

    private boolean transferByChunks = false;
    private int contentLength = -1;

    public HTTPResponse() {
        this.headers = new ArrayList<>();
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void addHeader(String header) {
        this.headers.add(header);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public boolean isTransferByChunks() {
        return transferByChunks;
    }

    public void setTransferByChunks(boolean transferByChunks) {
        this.transferByChunks = transferByChunks;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] generateHttpResponseAsBytes() {
        StringBuilder sb = new StringBuilder();
        for (String header : headers) {
            sb.append(header);
        }
        sb.append("\r\n");

        ByteArray byteArray = new ByteArray();
        byteArray.append(sb.toString().getBytes());
        if (body != null) {
            byteArray.append(body).append(new byte[] {13, 10});
        }
        return byteArray.createBytes();
    }

    @Override
    public String toString() {
        return "[statusLine=" + statusLine + ", headers=String[" + headers.size() + "], body=" + (body == null ? null : "byte[" + body.length + "]") + "]";
    }
}
