package crop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import crop.MarkupStorage.FileEntry;
import crop.filters.BilinearFilter;
import crop.filters.ImageFilter;
import utils.FileUtils;

public class ImageProcessApp {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: " + ImageProcessApp.class.getSimpleName() + " <folder>");
            System.exit(-1);
        }

        File inputFolder = new File(args[0]);
        MarkupStorage storage = new MarkupStorage(inputFolder);
        List<FileEntry> boundariesList = storage.getAllBoundaries();

        System.out.println("Starting process files: " + boundariesList.size());
        for (int i = 0; i < boundariesList.size(); i++) {
            FileEntry fileEntry = boundariesList.get(i);
            System.out.println("File " + (i + 1)  + " of " + boundariesList.size() + " - " + fileEntry.filename);
            processFile(inputFolder, fileEntry);
        }
        System.out.println("Done");
    }

    private static void processFile(File inputFolder, FileEntry fileEntry) throws IOException {
        String filename = fileEntry.filename;
        File inputFile = new File(inputFolder, filename);
        File outputFolder = new File(inputFolder, "crop");
        outputFolder.mkdir();

        BufferedImage srcImage = ImageIO.read(inputFile);
        ImageFilter filter = new BilinearFilter();

        ImageProcessor processor = new ImageProcessor();
        processor.setImage(srcImage);
        processor.setSelectionBoundaries(fileEntry.points);
        processor.setFilter(filter);

        BufferedImage newImage = processor.process();

        File outputFile = new File(outputFolder, filename);
        ImageIO.write(newImage, FileUtils.getFileExtension(filename), outputFile);
    }
}
