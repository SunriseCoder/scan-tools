package crop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import crop.filters.BilinearFilter;
import crop.filters.ImageFilter;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class ImageProcessApp {

    public static void main(String[] args) throws IOException {
        File inputFile = new File("C:\\tmp\\1.bmp");
        System.out.println("Opening image " + inputFile);
        BufferedImage srcImage = ImageIO.read(inputFile);
        Image image = SwingFXUtils.toFXImage(srcImage, null);
        List<Point2D> boundaries = createBoundaries();
        ImageFilter filter = new BilinearFilter();

        ImageProcessor processor = new ImageProcessor();
        processor.setImage(image);
        processor.setSelectionBoundaries(boundaries);
        processor.setFilter(filter);

        System.out.println("Processing start");
        BufferedImage newImage = processor.process();

        File outputFile = new File("C:\\tmp\\1p.bmp");
        ImageIO.write(newImage, "bmp", outputFile);
        System.out.println("Done");
    }

    private static List<Point2D> createBoundaries() {
        List<Point2D> boundaries = new ArrayList<>();

        String logData = "1.bmp;275.97051181050585,258.82927466632555;3850.9935496567773,284.0429881223372;3810.545754026179,4790.295212765457;230.9157285029214,4755.363025629941";
        String[] boundariesParts = logData.split(";");

        for (int i = 1; i < boundariesParts.length; i++) {
            String[] pointParts = boundariesParts[i].split(",");
            double x = Double.parseDouble(pointParts[0]);
            double y = Double.parseDouble(pointParts[1]);
            boundaries.add(new Point2D(x, y ));
        }

        return boundaries;
    }
}
