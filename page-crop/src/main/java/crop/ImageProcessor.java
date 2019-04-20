package crop;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class ImageProcessor {
    private Image image;
    private List<Point2D> selectionBoundaries;

    private double rotationAngle;

    public void setImage(Image image) {
        this.image = image;
    }

    public void setSelectionBoundaries(List<Point2D> boundaries) {
        this.selectionBoundaries = boundaries;
    }

    public BufferedImage process() {
        // Step 1 - Calculating rotation angle by the selection
        rotationAngle = calculateRotationAngle();

        // Step 2 - Calculating resolution of new Image
        NewImageBoundaries newImageBoundaries = calculateNewImageBoundaries();

        // Step 3 - Creating empty new Image
        BufferedImage sourceImage = SwingFXUtils.fromFXImage(image, null);
        BufferedImage newImage = new BufferedImage(newImageBoundaries.width, newImageBoundaries.height,
                sourceImage.getType());

        // Step 4 - Copying all pixels to the newImage
        copyRotatedPixels(sourceImage, newImage, rotationAngle, newImageBoundaries);

        // Step 5 - Crop rotated Image
        List<Point2D> cropBoundaries = rotatePoints(selectionBoundaries, -rotationAngle);
        // Applying offset
        cropBoundaries = cropBoundaries.stream()
            .map(point -> new Point2D(point.getX() - newImageBoundaries.minX, point.getY() - newImageBoundaries.minY))
            .collect(Collectors.toList());
        newImage = cropImage(newImage, cropBoundaries);

        return newImage;
    }

    private double calculateRotationAngle() {
        // TODO Currently this method uses only left edge of selection to calculate the angle
        // Maybe it worth to rewrite it, that it compare about top or left edge, and use the longest one
        Point2D cropPoint1 = selectionBoundaries.get(0);
        Point2D cropPoint2 = selectionBoundaries.get(3);

        double cropPointDeltaX = cropPoint2.getX() - cropPoint1.getX();
        double cropPointDeltaY = cropPoint2.getY() - cropPoint1.getY();

        double tan = cropPointDeltaX / cropPointDeltaY;
        double angle = -Math.atan(tan);
        return angle;
    }

    private NewImageBoundaries calculateNewImageBoundaries() {
        NewImageBoundaries result = new NewImageBoundaries();

        List<Point2D> sourceImageBoundaries = new ArrayList<>();
        sourceImageBoundaries.add(new Point2D(0, 0));
        sourceImageBoundaries.add(new Point2D(0, image.getWidth()));
        sourceImageBoundaries.add(new Point2D(image.getHeight(), image.getWidth()));
        sourceImageBoundaries.add(new Point2D(image.getHeight(), 0));

        List<Point2D> rotatedImageBoundaries = rotatePoints(sourceImageBoundaries, -rotationAngle);

        result.minX = (int) Math.floor(rotatedImageBoundaries.stream()
                        .mapToDouble(point -> point.getX()).min().getAsDouble());
        result.maxX = (int) Math.ceil(rotatedImageBoundaries.stream()
                .mapToDouble(point -> point.getX()).max().getAsDouble());
        result.minY = (int) Math.floor(rotatedImageBoundaries.stream()
                .mapToDouble(point -> point.getY()).min().getAsDouble());
        result.maxY = (int) Math.ceil(rotatedImageBoundaries.stream()
                .mapToDouble(point -> point.getY()).max().getAsDouble());

        result.width = result.maxX - result.minX + 1;
        result.height = result.maxY - result.minY + 1;

        return result;
    }

    private void copyRotatedPixels(BufferedImage sourceImage, BufferedImage newImage, double rotationAngle,
            NewImageBoundaries newImageBoundaries) {
        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                // Applying newImage offset before rotation
                Point2D sourcePoint = new Point2D(x + newImageBoundaries.minX, y + newImageBoundaries.minY);
                sourcePoint = rotatePoint(sourcePoint, rotationAngle);
                int color = getColorFromSourceImage(sourceImage, sourcePoint, newImageBoundaries);
                try {
                    newImage.setRGB(x, y, color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getColorFromSourceImage(BufferedImage sourceImage, Point2D point, NewImageBoundaries newImageBoundaries) {
        // TODO This should be rewritten to bilinear interpolation
        int color = 255 * 256 * 256 + 255 * 256 + 255; // White
        try {
            int x = (int) Math.round(point.getX());
            int y = (int) Math.round(point.getY());
            if (x == 0 && y == 0) {
                System.out.println();
            }
            color = sourceImage.getRGB(x , y);
        } catch (Exception e) {
            // Just using default color if the pixel is out of bounds
        }
        return color;
    }

    private BufferedImage cropImage(BufferedImage newImage, List<Point2D> cropBoundaries) {
        int minX = (int) Math.floor(cropBoundaries.stream().mapToDouble(point -> point.getX()).min().getAsDouble());
        int maxX = (int) Math.ceil(cropBoundaries.stream().mapToDouble(point -> point.getX()).max().getAsDouble());
        int minY = (int) Math.floor(cropBoundaries.stream().mapToDouble(point -> point.getY()).min().getAsDouble());
        int maxY = (int) Math.ceil(cropBoundaries.stream().mapToDouble(point -> point.getY()).max().getAsDouble());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        try {
            newImage = newImage.getSubimage(minX, minY, width, height);
        } catch (Exception e) {
            System.out.println();
            e.printStackTrace();
            throw new RuntimeException();
        }

        return newImage;
    }

    private List<Point2D> rotatePoints(List<Point2D> points, double angle) {
        List<Point2D> result = points.stream().map(point -> rotatePoint(point, angle)).collect(Collectors.toList());
        return result;
    }

    private Point2D rotatePoint(Point2D point, double angle) {
        double x = Math.cos(angle) * point.getX() - Math.sin(angle) * point.getY();
        double y = Math.sin(angle) * point.getX() + Math.cos(angle) * point.getY();
        Point2D rotatedPoint = new Point2D(x, y);
        return rotatedPoint;
    }

    private static class NewImageBoundaries {
        public int width;
        public int height;

        public int minX;
        public int maxX;
        public int minY;
        public int maxY;
    }
}
