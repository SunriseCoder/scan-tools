package processing;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import processing.reorder.ScanPagesReorder;

public class ScanPagesReorderApp {
    public static void main(String[] args) throws IOException {
        File inputFolder = new File("reorder/in");

        String[] filenames = inputFolder.list();
        int amountOfPages = filenames.length;

        ScanPagesReorder reorder = new ScanPagesReorder();
        List<Integer> reorderedPagesIndices = reorder.getReorderedList(amountOfPages);

        try (PrintWriter printWriter = new PrintWriter("reorder.bat");) {
            for (int i = 0; i < amountOfPages; i++) {
                Integer reorderedPagesIndex = reorderedPagesIndices.get(i);
                printWriter.println("copy \"reorder\\in\\" + filenames[reorderedPagesIndex - 1] + "\" \"reorder\\out\\"
                        + filenames[i] + "\"");
            }
            printWriter.flush();
        }
        System.out.println("Done");
    }
}
