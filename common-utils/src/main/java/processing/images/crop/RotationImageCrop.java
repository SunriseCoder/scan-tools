package processing.images.crop;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dto.Point;
import utils.MathUtils;

public class RotationImageCrop extends AbstractImageCrop {
    private BufferedImage sourceImage;
    private List<Point> selectionBoundaries;

    private double rotationAngle;

    @Override
    public BufferedImage processImage(BufferedImage image, List<Point> boundaries) {
        sourceImage = image;
        selectionBoundaries = boundaries;
        smoothFilter.setImage(sourceImage);

        // Step 1 - Calculating rotation angle by the selection
        rotationAngle = calculateRotationAngle();

        // Step 2 - Calculating resolution of new Image
        NewImageBoundaries newImageBoundaries = calculateNewImageBoundaries();

        // Step 3 - Creating empty new Image
        BufferedImage newImage = new BufferedImage(newImageBoundaries.width, newImageBoundaries.height,
                BufferedImage.TYPE_INT_RGB);

        // Step 4 - Copying all pixels to the newImage
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
        Point cropPoint1 = selectionBoundaries.get(0);
        Point cropPoint2 = selectionBoundaries.get(1);
        Point cropPoint3 = selectionBoundaries.get(2);
        Point cropPoint4 = selectionBoundaries.get(3);

        double angle1 = calculateRotationAngleHorizontalEdge(cropPoint1, cropPoint2);
        double angle2 = calculateRotationAngleVerticalEdge(cropPoint2, cropPoint3);
        double angle3 = calculateRotationAngleHorizontalEdge(cropPoint3, cropPoint4);
        double angle4 = calculateRotationAngleVerticalEdge(cropPoint4, cropPoint1);

        double angle = MathUtils.mathMeaning(angle1, angle2, angle3, angle4);
        return angle;
    }

    // TODO Refactor these 2 methods to get rid of duplication code
    private double calculateRotationAngleHorizontalEdge(Point cropPoint1, Point cropPoint2) {
        double cropPointDeltaX = cropPoint2.x - cropPoint1.x;
        double cropPointDeltaY = cropPoint2.y - cropPoint1.y;

        double tan = cropPointDeltaY / cropPointDeltaX;
        double angle = Math.atan(tan);
        return angle;
    }

    private double calculateRotationAngleVerticalEdge(Point cropPoint1, Point cropPoint2) {
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
                int color = smoothFilter.getRGB(sourcePoint);
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

    static class NewImageBoundaries {
        public int width;
        public int height;

        public int minX;
        public int maxX;
        public int minY;
        public int maxY;
    }
}
