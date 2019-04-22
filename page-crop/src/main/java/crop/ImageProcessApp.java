package crop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import crop.MarkupStorage.FileEntry;
import crop.filters.BilinearFilter;
import crop.filters.BinarizationFilter;
import crop.filters.ImageFilter;
import filters.FilenameFilterImages;
import utils.FileUtils;

public class ImageProcessApp {
    private static File inputFolder;
    private static File cropFolder;
    private static File resizedFolder;
    private static File binarizedFolder;
    private static File mergedFolder;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: " + ImageProcessApp.class.getSimpleName() + " <folder>");
            System.exit(-1);
        }

        inputFolder = new File(args[0]);
        cropFolder = new File(inputFolder, "crop");
        cropFolder.mkdir();
        resizedFolder = new File(cropFolder, "resized");
        resizedFolder.mkdir();
        binarizedFolder = new File(resizedFolder, "binarized");
        binarizedFolder.mkdir();
        mergedFolder = new File(binarizedFolder, "merged");
        mergedFolder.mkdir();

        MarkupStorage storage = new MarkupStorage(inputFolder);
        List<FileEntry> boundariesList = storage.getAllBoundaries();

        System.out.println("Starting process files: " + boundariesList.size());
        for (int i = 0; i < boundariesList.size(); i++) {
            FileEntry fileEntry = boundariesList.get(i);
            System.out.println("File " + (i + 1)  + " of " + boundariesList.size() + " - " + fileEntry.filename);

            // Crop Image
            cropImage(fileEntry);

            // Resize Image
            resizeImage(fileEntry);

            // Binarize Image
            binarizeImage(fileEntry);
        }

        System.out.println("Merging images");
        mergeImages();

        System.out.println("Done");
    }

    private static void cropImage(FileEntry fileEntry) throws IOException {
        String filename = fileEntry.filename;
        File inputFile = new File(inputFolder, filename);

        BufferedImage srcImage = ImageIO.read(inputFile);
        ImageFilter filter = new BilinearFilter();

        ImageProcessor processor = new ImageProcessor();
        processor.setImage(srcImage);
        processor.setSelectionBoundaries(fileEntry.points);
        processor.setFilter(filter);

        BufferedImage newImage = processor.process();

        File outputFile = new File(cropFolder, filename);
        ImageIO.write(newImage, FileUtils.getFileExtension(filename), outputFile);
    }

    private static void resizeImage(FileEntry fileEntry) throws IOException {
        String filename = fileEntry.filename;
        File inputFile = new File(cropFolder, filename);

        BufferedImage srcImage = ImageIO.read(inputFile);
        ImageFilter filter = new BilinearFilter();
        filter.setImage(srcImage);

        double factor = 400.0 / 600.0;
        int newWidth = (int) Math.round(srcImage.getWidth() * factor);
        int newHeight = (int) Math.round(srcImage.getHeight() * factor);
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                double srcX = x / factor;
                double srcY = y / factor;
                int resultColor = filter.getRGB(srcX, srcY);
                newImage.setRGB(x, y, resultColor);
            }
        }

        File outputFile = new File(resizedFolder, filename);
        ImageIO.write(newImage, FileUtils.getFileExtension(filename), outputFile);
    }

    private static void binarizeImage(FileEntry fileEntry) throws IOException {
        String filename = fileEntry.filename;
        File inputFile = new File(resizedFolder, filename);

        BufferedImage srcImage = ImageIO.read(inputFile);
        ImageFilter filter = new BinarizationFilter();
        filter.setImage(srcImage);

        BufferedImage newImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                int resultColor = filter.getRGB(x, y);
                newImage.setRGB(x, y, resultColor);
            }
        }

        File outputFile = new File(binarizedFolder, filename);
        ImageIO.write(newImage, FileUtils.getFileExtension(filename), outputFile);
    }

    private static void mergeImages() throws IOException {
        File[] files = binarizedFolder.listFiles(new FilenameFilterImages());
        int counter = 1;
        BufferedImage image = ImageIO.read(files[0]);
        BufferedImage resultImage = mergeImages(null, image);
        saveImage(resultImage, mergedFolder, "Scan-" + String.format("%03d", counter++) + ".bmp");
        for (int i = 1; i < files.length - 1; i += 2) {
            File file1 = files[i];
            File file2 = files[i + 1];
            System.out.println("Merging file " + (i + 1)  + " of " + files.length + " - " +
                    file1.getName() + " and " + file2.getName());
            BufferedImage image1 = ImageIO.read(file1);
            BufferedImage image2 = ImageIO.read(file2);
            resultImage = mergeImages(image1, image2);
            saveImage(resultImage, mergedFolder, "Scan-" + String.format("%03d", counter++) + ".bmp");
        }
        image = ImageIO.read(files[files.length - 1]);
        resultImage = mergeImages(image, null);
        saveImage(resultImage, mergedFolder, "Scan-" + String.format("%03d", counter++) + ".bmp");
    }

    private static void saveImage(BufferedImage image, File folder, String filename) throws IOException {
        File outputFile = new File(folder, filename);
        ImageIO.write(image, FileUtils.getFileExtension(filename), outputFile);
    }

    private static BufferedImage mergeImages(BufferedImage image1, BufferedImage image2) {
        if (image1 == null) {
            image1 = createWhiteEmptyImage(image2.getWidth(), image2.getHeight());
        }
        if (image2 == null) {
            image2 = createWhiteEmptyImage(image1.getWidth(), image1.getHeight());
        }

        int width = image1.getWidth() + image2.getWidth();
        int height = Math.max(image1.getHeight(), image2.getHeight());
        BufferedImage newImage = createWhiteEmptyImage(width, height);

        int offsetX = 0;
        int offsetY = (newImage.getHeight() - image1.getHeight()) / 2;
        int[] data = image1.getRGB(0, 0, image1.getWidth(), image1.getHeight(), null, 0, image1.getWidth());
        newImage.setRGB(offsetX, offsetY, image1.getWidth(), image1.getHeight(), data, 0, image1.getWidth());

        offsetX = image1.getWidth();
        offsetY = (newImage.getHeight() - image2.getHeight()) / 2;
        data = image2.getRGB(0, 0, image2.getWidth(), image2.getHeight(), null, 0, image2.getWidth());
        newImage.setRGB(offsetX, offsetY, image2.getWidth(), image2.getHeight(), data, 0, image2.getWidth());

        return newImage;
    }

    private static BufferedImage createWhiteEmptyImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        return image;
    }
}
