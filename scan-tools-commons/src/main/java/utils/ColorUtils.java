package utils;

import structures.RGB;

public class ColorUtils {

    public static int getRGB(int r, int g, int b) {
        int a = 255;
        int rgb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
        return rgb;
    }

    public static int getRGB(RGB rgb) {
        int r = Math.min((int) Math.round(rgb.r), 255);
        int g = Math.min((int) Math.round(rgb.g), 255);
        int b = Math.min((int) Math.round(rgb.b), 255);

        int result = getRGB(r, g, b);

        return result;
    }

    public static RGB multiplyRGB(int rgb, double factor) {
        RGB resultRGB = new RGB();

        resultRGB.r = (rgb >> 16 & 0xFF) * factor;
        resultRGB.g = (rgb >> 8 & 0xFF) * factor;
        resultRGB.b = (rgb & 0xFF) * factor;

        return resultRGB;
    }
}
