package rename;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Map;

public class FileRenameApp {

    private static final String CREATION_TIME_ATTRIBUTE = "creationTime";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }

        File folder = new File(args[0]);
        File[] files = folder.listFiles();

        try (PrintWriter renameWriter = new PrintWriter("rename.bat");
                PrintWriter undoWriter = new PrintWriter("undo-rename.bat")) {
            for (File file : files) {
                String oldFileName = file.getName();
                System.out.println("Processing file: " + oldFileName);

                Map<String, Object> attributes = Files.readAttributes(file.toPath(), CREATION_TIME_ATTRIBUTE);
                FileTime creationTime = (FileTime) attributes.get(CREATION_TIME_ATTRIBUTE);

                String newFileName = creationTime.toString().substring(0, 10) + "_" + oldFileName;
                String renameCommand = "rename \"" + oldFileName + "\" \"" + newFileName + "\"";
                renameWriter.println(renameCommand);

                String undoRenameCommand = "rename \"" + newFileName + "\" \"" + oldFileName + "\"";
                undoWriter.println(undoRenameCommand);
            }
        }

        System.out.println("Batch files generated");
    }

    private static void printUsage() {
        System.out.println("Usage: " + FileRenameApp.class.getName() + " <folder>\n"
                + "\t where <folder> is a path, where the files need to be renamed are present.\n"
                + "Warning! Rename ALL the files in the <folder>");
    }
}
