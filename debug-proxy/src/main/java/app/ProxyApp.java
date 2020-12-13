package app;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import util.RequestHandler;

public class ProxyApp {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: " + ProxyApp.class.getName() + " <listening port>");
            System.exit(-1);
        }

        int listeningPort = Integer.parseInt(args[0]);
        @SuppressWarnings("resource")
        ServerSocket ss = new ServerSocket(listeningPort);

        File requestDumpFolder = new File("request-dumps");
        if (!requestDumpFolder.exists()) {
            requestDumpFolder.mkdir();
        }

        System.out.println("Proxy started");

        while (true) {
            Socket s = ss.accept();
            RequestHandler requestHandler = new RequestHandler(s);
            requestHandler.handle();
        }
    }
}
