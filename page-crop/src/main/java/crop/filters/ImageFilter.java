package crop.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

import crop.dto.Point;

public interface ImageFilter {
    void setImage(BufferedImage image);
    void setDefaultColor(Color color);
    int getRGB(Point point);
    int getRGB(double x, double y);
}
