package reorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ScanPagesReorderApp {
    public static void main(String[] args) throws IOException {
        File inputFolder = new File("data/in");
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
        	System.out.println("Input folder '" + inputFolder.getAbsolutePath() + "' does not exist.");
        	System.exit(-1);
        }

        String[] filenames = inputFolder.list();
        int amountOfPages = filenames.length;

        ScanPagesReorder reorder = new ScanPagesReorder();
        List<Integer> reorderedPagesIndices = reorder.getReorderedList(amountOfPages);

        try (PrintWriter printWriter = new PrintWriter("data/reorder.bat");) {
            for (int i = 0; i < amountOfPages; i++) {
                Integer reorderedPagesIndex = reorderedPagesIndices.get(i);
                String line = "copy \"in\\" + filenames[i] + "\" \"out\\"
                        + filenames[reorderedPagesIndex - 1] + "\"";
                printWriter.println(line);
                System.out.println(line);
            }
            printWriter.flush();
        }
        System.out.println("Done");
    }
}
