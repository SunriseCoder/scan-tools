package files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import filters.ExclusiveFilenameFilter;

public class ListForFFMpegConcat {

    public static void main(String[] args) throws IOException {
        System.out.println("Preparing video list for ffmpeg concat...");

        File currentFolder = new File(".");

        ExclusiveFilenameFilter filter = new ExclusiveFilenameFilter("bat", "txt");
        String[] filenames = currentFolder.list(filter);

        try (PrintWriter printWriter = new PrintWriter("files.txt")) {
            for (String filename : filenames) {
                String line = "file '" + filename + "'";
                System.out.println(line);
                printWriter.println(line);
            }
        }

        System.out.println("Done");
    }
}
