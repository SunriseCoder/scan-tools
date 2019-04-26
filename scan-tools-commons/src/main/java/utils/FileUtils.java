package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

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
        FileWriter fileWriter = new FileWriter(file, true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(text);
        printWriter.close();
    }

    public static Parent loadFXML(Object object) throws IOException {
        String resourceName = object.getClass().getSimpleName() + ".fxml";
        URL resource = object.getClass().getResource(resourceName);
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setController(object);
        Parent root = loader.load();
        return root;
    }

    public static void copyFiles(File sourceFile, File destinationFile) throws IOException {
        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
