package process.processing.render.binarization;

import java.awt.image.BufferedImage;

import process.processing.render.filters.ImageFilter;

public class ImageBinarization {
    private ImageFilter imageFilter;

    public void setColorFilter(ImageFilter imageFilter) {
        this.imageFilter = imageFilter;
    }

    public BufferedImage processImage(BufferedImage image) {
        imageFilter.setImage(image);

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                int resultColor = imageFilter.getRGB(x, y);
                newImage.setRGB(x, y, resultColor);
            }
        }

        return newImage;
    }
}
