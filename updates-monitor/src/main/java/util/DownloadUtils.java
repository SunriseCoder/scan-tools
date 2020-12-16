package util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adaptors.ByteArray;

public class DownloadUtils {

    public static Response downloadPage(String urlString, Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        if (headers != null) {
            for (Entry<String, String> headerEntry : headers.entrySet()) {
                connection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        ByteArray byteArray = new ByteArray();
        try (InputStream is = connection.getInputStream();) {
            int read = 0;
            byte[] buffer = new byte[65536];
            while (read > -1) {
                read = is.read(buffer);
                if (read > 0) {
                    byteArray.append(buffer, 0, read);
                }
            }
        }

        Response response = new Response();
        response.headers = connection.getHeaderFields();
        response.body = byteArray.createString();
        return response;
    }

    public static class Response {
        public Map<String, List<String>> headers;
        public String body;
    }
}
