package rename;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.regex.Pattern;

public class FileRenameApp {
    private static final Pattern FILE_PATTERN = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}_.*$");

    private static String fileTimeAttribute = "creationTime";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }

        File folder = new File(args[0]);

        if (args.length >= 2) {
            fileTimeAttribute = args[1];
        }

        try (PrintWriter renameWriter = new PrintWriter("rename.bat");
                PrintWriter undoWriter = new PrintWriter("undo-rename.bat")) {
            File rootFolder = folder.getAbsoluteFile().getParentFile();
            scanFolderRecursively(rootFolder, folder.getAbsoluteFile(), renameWriter, undoWriter);
        }

        System.out.println("Batch files generated");
    }

    private static void scanFolderRecursively(File root, File folder, PrintWriter renameWriter, PrintWriter undoWriter)
            throws IOException {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanFolderRecursively(root, file, renameWriter, undoWriter);
                continue;
            }

            String oldFileName = file.getName();
            // If the File already has "YYYY-MM-DD_*" Pattern, don't rename it
            if (FILE_PATTERN.matcher(oldFileName).matches()) {
                continue;
            }

            String oldFileRelativeName = root.toPath().relativize(file.toPath()).toString();
            System.out.println("Processing file: " + oldFileRelativeName);

            Map<String, Object> attributes = Files.readAttributes(file.toPath(), fileTimeAttribute);
            FileTime creationTime = (FileTime) attributes.get(fileTimeAttribute);

            String newFileName = creationTime.toString().substring(0, 10) + "_" + oldFileName;
            File newFile = new File(file.getParentFile(), newFileName);
            String newFileRelativeName = root.toPath().relativize(newFile.toPath()).toString();
            String renameCommand = "move \"" + oldFileRelativeName + "\" \"" + newFileRelativeName + "\"";
            renameWriter.println(renameCommand);

            String undoRenameCommand = "move \"" + newFileRelativeName + "\" \"" + oldFileRelativeName + "\"";
            undoWriter.println(undoRenameCommand);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: " + FileRenameApp.class.getName() + " <folder> [file_time_attribute]\n"
                + "\t where\n"
                + "\t\t <folder> is a path, where the files need to be renamed are present.\n"
                + "\t\t [file_time_attribute] is a file attribute to rename the file.\n"
                + "\t\t\t run " + FileAttributesDumpApp.class.getName() + " to see the list of attributes.\n"
                + "Warning! Rename ALL the files in the <folder>");
    }
}
