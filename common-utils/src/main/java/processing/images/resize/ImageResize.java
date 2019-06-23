package processing.images.resize;

import java.awt.image.BufferedImage;

import processing.images.filters.ImageFilter;

public class ImageResize {
    private ImageFilter smoothFilter;
    private int sourceDPI;
    private int targetDPI;

    public void setSmoothFilter(ImageFilter smoothFilter) {
        this.smoothFilter = smoothFilter;
    }

    public void setSourceDPI(int sourceDPI) {
        this.sourceDPI = sourceDPI;
    }

    public void setTargetDPI(int targetDPI) {
        this.targetDPI = targetDPI;
    }

    public BufferedImage processImage(BufferedImage srcImage) {
        smoothFilter.setImage(srcImage);

        double factor = (double) targetDPI / sourceDPI;
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
