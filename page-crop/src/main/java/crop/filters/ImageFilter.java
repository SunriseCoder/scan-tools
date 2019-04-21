package crop.filters;

import java.awt.image.BufferedImage;

import javafx.geometry.Point2D;

public interface ImageFilter {
    void setImage(BufferedImage sourceImage);
    int getColor(Point2D sourcePoint);
}
