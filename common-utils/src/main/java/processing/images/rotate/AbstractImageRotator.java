package processing.images.rotate;

import java.awt.image.BufferedImage;
import java.util.List;

import dto.Point;
import processing.images.filters.ImageFilter;

public abstract class AbstractImageRotator {
    public abstract void setSmoothFilter(ImageFilter smoothFilter);
    public abstract BufferedImage processImage(BufferedImage image, List<Point> markupBoundaries);
    public abstract List<Point> calculateCropBoundaries(BufferedImage image, List<Point> markupBoundaries);
}
