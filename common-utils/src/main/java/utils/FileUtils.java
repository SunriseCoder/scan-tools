package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
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

    public static File createFile(String absoluteFilePath, boolean overwrite) throws IOException {
        File file = new File(absoluteFilePath);

        createFile(file, overwrite);

        return file;
    }
    public static void createFile(File file, boolean overwrite) throws IOException {
        file.getParentFile().mkdirs();

        if (!overwrite && file.exists()) {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }

        if (overwrite && file.exists()) {
            file.delete();
        }

        file.createNewFile();
    }

    public static File createFile(String foldername, String filename, boolean overwrite) throws IOException {
        File folder = new File(foldername);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!folder.isDirectory()) {
            throw new FileNotFoundException("'" + folder.getAbsolutePath() + "' is not a directory");
        }
        File file = new File(folder, filename);
        if (!overwrite && file.exists()) {
            throw new FileAlreadyExistsException("'" + filename + "' in '" + foldername + "'");
        }
        if (overwrite && file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    public static File checkAndGetFile(String foldername, String filename) throws FileNotFoundException {
        File folder = new File(foldername);
        File file = new File(folder, filename);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException("'" + filename + "' in '" + foldername + "'");
        }
        return file;
    }

    public static File checkAndGetFile(String absoluteFileName) throws FileNotFoundException {
        File file = new File(absoluteFileName);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException(absoluteFileName);
        }
        return file;
    }

    public static File getExistingParentIfFolderNotExists(File folder) {
        if (folder == null || (folder.exists() && folder.isDirectory())) {
            return folder;
        }
        
        File parent = folder.getParentFile();
        parent = getExistingParentIfFolderNotExists(parent);
        return parent;
    }

    public static void saveToFile(String text, String filename) throws IOException {
        File file = createFile(filename, true);

        try (FileWriter fileWriter = new FileWriter(file, true);
                PrintWriter printWriter = new PrintWriter(fileWriter);) {
            printWriter.println(text);
        }
    }

    public static String replaceResolution(String filename, String replacement) {
        String newFileName = getFileName(filename);
        newFileName += "." + replacement;
        return newFileName;
    }
}
