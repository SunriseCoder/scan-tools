package process.processing.preprocessing.rotation;

import java.awt.image.BufferedImage;

public class RotationOdd180Degrees extends AbstractRotator {

    @Override
    public BufferedImage rotateImage(BufferedImage image, int index) {
        // Rotating Odd pages based human numbering (1, 2, 3, etc)
        if (index % 2 == 1) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage rotatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                rotatedImage.setRGB(width - x - 1, height - y - 1, rgb);
            }
        }

        return rotatedImage;
    }
}
