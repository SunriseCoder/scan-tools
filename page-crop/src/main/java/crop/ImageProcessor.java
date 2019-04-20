package crop;

import java.awt.image.BufferedImage;
import java.util.List;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class ImageProcessor {
    private Image image;
    private List<Point2D> boundaries;

    public void setImage(Image image) {
        this.image = image;
    }

    public void setBoundaries(List<Point2D> boundaries) {
        this.boundaries = boundaries;
    }

    public BufferedImage process() {
        // TODO Auto-generated method stub
        return SwingFXUtils.fromFXImage(image, null);
    }
}
