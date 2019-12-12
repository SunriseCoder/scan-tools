package processing.images.split;

import java.awt.image.BufferedImage;

public abstract class AbstractSplitter {
    public abstract BufferedImage[] split(BufferedImage image);
}
