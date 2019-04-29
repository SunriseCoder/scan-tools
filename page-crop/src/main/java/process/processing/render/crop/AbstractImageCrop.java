package process.processing.render.crop;

import java.awt.image.BufferedImage;
import java.util.List;

import process.dto.Point;
import process.processing.render.filters.ImageFilter;

public abstract class AbstractImageCrop {
    protected ImageFilter smoothFilter;

    public void setSmoothFilter(ImageFilter smoothFilter) {
        this.smoothFilter = smoothFilter;
    }

    public abstract BufferedImage processImage(BufferedImage image, List<Point> boundaries);
}
