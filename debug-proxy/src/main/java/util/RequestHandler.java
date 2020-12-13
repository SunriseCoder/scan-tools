package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import dto.HTTPRequest;
import dto.HTTPResponse;

public class RequestHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss-SSS");

    private Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void handle() {
        Date requestTime = new Date();
        System.out.println("=============================");
        System.out.println(DATE_FORMAT.format(requestTime));

        Socket targetServerSocket = null;

        try {
            // Fetching Client Request
            HTTPRequest clientRequest = HTTPRequestHelper.fetch(clientSocket.getInputStream());
            System.out.println("ClientRequest: " + clientRequest);

            // Establishing Connection with Server
            InetAddress remoteAddress = InetAddress.getByName(clientRequest.getHost());
            /*System.out.println("Forwarding " + clientRequest.getMethod() + " request to: " + remoteAddress + ":"
                    + clientRequest.getPort() + " " + clientRequest.getPath());*/
            if (clientRequest.isSsl()) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket remoteSSLSocket = (SSLSocket) factory.createSocket(remoteAddress, clientRequest.getPort());
                remoteSSLSocket.startHandshake();
                targetServerSocket = remoteSSLSocket;
            } else {
                targetServerSocket = new Socket(remoteAddress, clientRequest.getPort());
            }
            targetServerSocket.setSoTimeout(5000);

            // Forwarding Client Request to Server
            FileOutputStream fileOutputStream = new FileOutputStream("request-dumps/" + DATE_FORMAT.format(requestTime) + "-request.dat");
            List<OutputStream> outputStreams = new ArrayList<>();
            outputStreams.add(targetServerSocket.getOutputStream());
            outputStreams.add(fileOutputStream);
            HTTPRequestHelper.send(clientRequest, outputStreams);
            fileOutputStream.close();

            // Forwarding Server Response
            fileOutputStream = new FileOutputStream("request-dumps/" + DATE_FORMAT.format(requestTime) + "-response.dat");
            outputStreams = new ArrayList<>();
            outputStreams.add(clientSocket.getOutputStream());
            outputStreams.add(fileOutputStream);
            HTTPResponse serverResponse = HTTPResponseHelper.forward(clientRequest, targetServerSocket.getInputStream(), outputStreams);
            fileOutputStream.close();

            System.out.println("RemoteResponse: " + serverResponse);
            System.out.println("=============================");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Closing sockets
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (targetServerSocket != null && !targetServerSocket.isClosed()) {
                    targetServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
