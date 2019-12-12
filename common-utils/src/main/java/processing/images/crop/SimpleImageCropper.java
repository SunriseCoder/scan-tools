package processing.images.crop;

import java.awt.image.BufferedImage;
import java.util.List;

import dto.Point;

public class SimpleImageCropper extends AbstractImageCropper {

    @Override
    public BufferedImage processImage(BufferedImage image, List<Point> boundaries) {
        int minX = (int) Math.floor(boundaries.stream().mapToDouble(point -> point.x).min().getAsDouble());
        int maxX = (int) Math.ceil(boundaries.stream().mapToDouble(point -> point.x).max().getAsDouble());
        int minY = (int) Math.floor(boundaries.stream().mapToDouble(point -> point.y).min().getAsDouble());
        int maxY = (int) Math.ceil(boundaries.stream().mapToDouble(point -> point.y).max().getAsDouble());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        BufferedImage newImage = image.getSubimage(minX, minY, width, height);
        return newImage;
    }
}
