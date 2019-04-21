package crop.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javafx.geometry.Point2D;

public interface ImageFilter {
    void setImage(BufferedImage image);
    void setDefaultColor(Color color);
    int getRGB(Point2D point);
    int getRGB(double x, double y);
}
