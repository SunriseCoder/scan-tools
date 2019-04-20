package rotate;

import java.awt.image.BufferedImage;

public class ScanPagesRotator {

    public BufferedImage rotatePage(BufferedImage sourceImage, String rotationAngleClockwise) {
        BufferedImage rotatedImage;
        switch (rotationAngleClockwise) {
            case "180":
                rotatedImage = rotate180Degrees(sourceImage);
                break;
            default:
                throw new IllegalArgumentException(
                        "Rotation to " + rotationAngleClockwise + " degrees clockwise is not supported");
        }
        return rotatedImage;
    }

    private BufferedImage rotate180Degrees(BufferedImage sourceImage) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        BufferedImage rotatedImage = new BufferedImage(width, height, sourceImage.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = sourceImage.getRGB(x, y);
                rotatedImage.setRGB(width - x - 1, height - y - 1, rgb);
            }
        }
        return rotatedImage;
    }
}
