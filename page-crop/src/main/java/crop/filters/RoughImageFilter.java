package crop.filters;

import java.awt.image.BufferedImage;

import javafx.geometry.Point2D;

public class RoughImageFilter implements ImageFilter {
    private BufferedImage image;
    private int defaultColor;

    public RoughImageFilter() {
        // White
        defaultColor = 255 * 256 * 256 + 255 * 256 + 255;
    }

    @Override
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public int getColor(Point2D point) {
        int x = (int) Math.round(point.getX());
        int y = (int) Math.round(point.getY());

        int result;
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            result = image.getRGB(x, y);
        } else {
            result = defaultColor;
        }

        return result;
    }
}
