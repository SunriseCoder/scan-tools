package crop.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javafx.geometry.Point2D;

public abstract class AbstractImageFilter implements ImageFilter {
    private BufferedImage image;
    private Color defaultColor;

    public AbstractImageFilter() {
        defaultColor = Color.WHITE;
    }

    @Override
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void setDefaultColor(Color color) {
        defaultColor = color;
    }

    @Override
    public int getRGB(Point2D point) {
        return getRGB(point.getX(), point.getY());
    }

    protected int getRGBFromImage(int x, int y) {
        int result;
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            result = image.getRGB(x, y);
        } else {
            result = defaultColor.getRGB();
        }
        return result;
    }
}
