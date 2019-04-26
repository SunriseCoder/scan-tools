package process.processing.preprocessing.rotation;

import java.awt.image.BufferedImage;

public abstract class AbstractRotator {
    public abstract BufferedImage rotateImage(BufferedImage image, int index);
}
