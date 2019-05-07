package process.processing.prepare.rotation;

import java.awt.image.BufferedImage;

public abstract class AbstractRotator {
    public abstract BufferedImage rotateImage(BufferedImage image, int index);
}
