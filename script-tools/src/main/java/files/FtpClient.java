package files;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

public class FtpClient {
    private FTPSClient ftp;

    public void open(InetAddress server, int port, String user, String password) throws IOException {
        ftp = new FTPSClient(true);

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server, port);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.login(user, password);

        ftp.execPBSZ(0);
        ftp.execPROT("P");
        ftp.enterLocalPassiveMode();
        ftp.enterRemotePassiveMode();
    }

    public void close() throws IOException {
        ftp.disconnect();
    }

    public void listFiles(String ftpFolder) throws IOException {
        FTPFile[] files = ftp.listFiles("/Giridhari-User");
        List<String> names = Arrays.stream(files).map(FTPFile::getName).collect(Collectors.toList());
        System.out.println(names);
    }
}
