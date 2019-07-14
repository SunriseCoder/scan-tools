package files;

import java.io.File;
import java.net.InetAddress;

public class CopyHugeFiles {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }

        int position = 0;
        File sourceFile = new File(args[position++]);

        InetAddress server = InetAddress.getByName(args[position++]);
        Integer port = Integer.valueOf(args[position++]);
        String user = args[position++];
        String password = args[position++];

        String ftpFolder = args[position++];

        FtpClient ftpClient = new FtpClient();
        ftpClient.open(server, port, user, password);

        ftpClient.listFiles(ftpFolder);

        ftpClient.close();
    }

    private static void printUsage() {
        System.out.println("Usage: " + CopyHugeFiles.class.getName() + " <source-file> <target-folder> <split-part-size> <pause-after-size>\n"
                + "\t where\n"
                + "\t\t <source-file> is a file to be copied\n"
                + "\t\t <target-folder> is a folder, where all parts of the file should be temporary saved\n"
                // TODO finish this
                + "");
    }
}
