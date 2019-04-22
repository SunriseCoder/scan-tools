package crop;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import crop.dto.Point;
import crop.filters.ImageFilter;

public class ImageProcessor {
    private BufferedImage sourceImage;
    private List<Point> selectionBoundaries;
    private ImageFilter filter;

    private double rotationAngle;

    public void setImage(BufferedImage image) {
        sourceImage = image;
    }

    public void setSelectionBoundaries(List<Point> boundaries) {
        this.selectionBoundaries = boundaries;
    }

    public void setFilter(ImageFilter filter) {
        this.filter = filter;
    }

    public BufferedImage process() {
        // Step 1 - Calculating rotation angle by the selection
        rotationAngle = calculateRotationAngle();

        // Step 2 - Calculating resolution of new Image
        NewImageBoundaries newImageBoundaries = calculateNewImageBoundaries();

        // Step 3 - Creating empty new Image
        BufferedImage newImage = new BufferedImage(newImageBoundaries.width, newImageBoundaries.height,
                BufferedImage.TYPE_INT_RGB);

        // Step 4 - Copying all pixels to the newImage
        filter.setImage(sourceImage);
        copyRotatedPixels(newImage, rotationAngle, newImageBoundaries);

        // Step 5 - Crop rotated Image
        List<Point> cropBoundaries = rotatePoints(selectionBoundaries, -rotationAngle);
        // Applying offset
        cropBoundaries = cropBoundaries.stream()
            .map(point -> new Point(point.x - newImageBoundaries.minX, point.y - newImageBoundaries.minY))
            .collect(Collectors.toList());
        newImage = cropImage(newImage, cropBoundaries);

        return newImage;
    }

    private double calculateRotationAngle() {
        // TODO Currently this method uses only left edge of selection to calculate the angle
        // Maybe it worth to rewrite it, that it compare about top or left edge, and use the longest one
        Point cropPoint1 = selectionBoundaries.get(0);
        Point cropPoint2 = selectionBoundaries.get(3);

        double cropPointDeltaX = cropPoint2.x - cropPoint1.x;
        double cropPointDeltaY = cropPoint2.y - cropPoint1.y;

        double tan = cropPointDeltaX / cropPointDeltaY;
        double angle = -Math.atan(tan);
        return angle;
    }

    private NewImageBoundaries calculateNewImageBoundaries() {
        NewImageBoundaries result = new NewImageBoundaries();

        List<Point> sourceImageBoundaries = new ArrayList<>();
        sourceImageBoundaries.add(new Point(0, 0));
        sourceImageBoundaries.add(new Point(sourceImage.getWidth(), 0));
        sourceImageBoundaries.add(new Point(sourceImage.getWidth(), sourceImage.getHeight()));
        sourceImageBoundaries.add(new Point(0, sourceImage.getHeight()));

        List<Point> rotatedImageBoundaries = rotatePoints(sourceImageBoundaries, -rotationAngle);

        result.minX = (int) Math.floor(rotatedImageBoundaries.stream()
                        .mapToDouble(point -> point.x).min().getAsDouble());
        result.maxX = (int) Math.ceil(rotatedImageBoundaries.stream()
                .mapToDouble(point -> point.x).max().getAsDouble());
        result.minY = (int) Math.floor(rotatedImageBoundaries.stream()
                .mapToDouble(point -> point.y).min().getAsDouble());
        result.maxY = (int) Math.ceil(rotatedImageBoundaries.stream()
                .mapToDouble(point -> point.y).max().getAsDouble());

        result.width = result.maxX - result.minX + 1;
        result.height = result.maxY - result.minY + 1;

        return result;
    }

    private void copyRotatedPixels(BufferedImage newImage, double rotationAngle, NewImageBoundaries newImageBoundaries) {
        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                // Applying newImage offset before rotation
                Point sourcePoint = new Point(x + newImageBoundaries.minX, y + newImageBoundaries.minY);
                sourcePoint = rotatePoint(sourcePoint, rotationAngle);
                int color = filter.getRGB(sourcePoint);
                try {
                    newImage.setRGB(x, y, color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private BufferedImage cropImage(BufferedImage newImage, List<Point> cropBoundaries) {
        int minX = (int) Math.floor(cropBoundaries.stream().mapToDouble(point -> point.x).min().getAsDouble());
        int maxX = (int) Math.ceil(cropBoundaries.stream().mapToDouble(point -> point.x).max().getAsDouble());
        int minY = (int) Math.floor(cropBoundaries.stream().mapToDouble(point -> point.y).min().getAsDouble());
        int maxY = (int) Math.ceil(cropBoundaries.stream().mapToDouble(point -> point.y).max().getAsDouble());

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

    private List<Point> rotatePoints(List<Point> points, double angle) {
        List<Point> result = points.stream().map(point -> rotatePoint(point, angle)).collect(Collectors.toList());
        return result;
    }

    private Point rotatePoint(Point point, double angle) {
        double x = Math.cos(angle) * point.x - Math.sin(angle) * point.y;
        double y = Math.sin(angle) * point.x + Math.cos(angle) * point.y;
        Point rotatedPoint = new Point(x, y);
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
