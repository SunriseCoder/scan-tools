package process.processing.render.resize;

import java.awt.image.BufferedImage;

import process.processing.render.filters.ImageFilter;

public class ImageResize {
    private ImageFilter smoothFilter;

    public void setSmoothFilter(ImageFilter smoothFilter) {
        this.smoothFilter = smoothFilter;
    }

    public BufferedImage processImage(BufferedImage srcImage, int sourceDPI, int targetDPI) {
        smoothFilter.setImage(srcImage);

        double factor = 400.0 / 600.0;
        int newWidth = (int) Math.round(srcImage.getWidth() * factor);
        int newHeight = (int) Math.round(srcImage.getHeight() * factor);
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                double srcX = x / factor;
                double srcY = y / factor;
                int resultColor = smoothFilter.getRGB(srcX, srcY);
                newImage.setRGB(x, y, resultColor);
            }
        }

        return newImage;
    }
}
