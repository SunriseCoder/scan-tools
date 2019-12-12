package processing.images.crop;

import java.awt.image.BufferedImage;
import java.util.List;

import dto.Point;

public abstract class AbstractImageCropper {
    public abstract BufferedImage processImage(BufferedImage image, List<Point> boundaries);
}
