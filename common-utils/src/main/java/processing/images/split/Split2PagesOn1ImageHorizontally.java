package processing.images.split;

import java.awt.image.BufferedImage;

public class Split2PagesOn1ImageHorizontally extends AbstractSplitter {

    @Override
    public BufferedImage[] split(BufferedImage image) {
        int width = image.getWidth() / 2;
        int height = image.getHeight();

        // 2 Images
        BufferedImage[] splittedImages = new BufferedImage[2];
        for (int i = 0; i < 2; i++) {
            BufferedImage splittedImage = image.getSubimage(i * width, 0, width, height);
            splittedImages[i] = splittedImage;
        }

        return splittedImages;
    }
}
