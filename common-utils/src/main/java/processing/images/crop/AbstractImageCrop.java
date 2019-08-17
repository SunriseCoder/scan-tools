package processing.images.crop;

import java.awt.image.BufferedImage;
import java.util.List;

import dto.Point;
import processing.images.filters.ImageFilter;

public abstract class AbstractImageCrop {
    protected ImageFilter smoothFilter;

    public void setSmoothFilter(ImageFilter smoothFilter) {
        this.smoothFilter = smoothFilter;
    }

    public abstract BufferedImage processImage(BufferedImage image, List<Point> boundaries);
}
