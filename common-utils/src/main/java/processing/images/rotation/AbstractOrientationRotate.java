package processing.images.rotation;

import java.awt.image.BufferedImage;

public abstract class AbstractOrientationRotate {
    public abstract BufferedImage rotateImage(BufferedImage image, int index);
}
