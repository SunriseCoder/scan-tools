package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {

    public static String getFileExtension(String filename) {
        int positionOfLastDot = filename.lastIndexOf(".");
        String format = filename.substring(positionOfLastDot + 1);
        return format;
    }

    public static String getFileName(String filename) {
        int positionOfLastDot = filename.lastIndexOf(".");
        String name = filename.substring(0, positionOfLastDot);
        return name;
    }

    public static void printLine(File folder, String filename, String text) throws IOException {
        File file = new File(folder, filename);
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.println(text);
        printWriter.close();
    }
}
