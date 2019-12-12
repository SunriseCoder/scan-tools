package processing.images.rotation;

import java.awt.image.BufferedImage;

public class RotationAll90DegreesClockWise extends AbstractOrientationRotate {

    @Override
    public BufferedImage rotateImage(BufferedImage image, int index) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                rotatedImage.setRGB((height - 1 ) - y, x, rgb);
            }
        }

        return rotatedImage;
    }
}
