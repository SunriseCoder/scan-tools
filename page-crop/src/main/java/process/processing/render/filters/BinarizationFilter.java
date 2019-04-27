package process.processing.render.filters;

import java.awt.Color;

import utils.ColorUtils;

public class BinarizationFilter extends AbstractImageFilter {
    private double threshold = 85 * 5 * 1000;
    private double rw = 3;
    private double gw = 1;
    private double bw = 1;

    @Override
    public int getRGB(double x, double y) {
        int sourceColor = getRGBFromImage((int) x, (int) y);

        double r = ColorUtils.getRed(sourceColor) * rw;
        double g = ColorUtils.getGreen(sourceColor) * gw;
        double b = ColorUtils.getBlue(sourceColor) * bw;
        double rgb = r * r + g * g + b * b;

        Color color = rgb >= threshold ? Color.WHITE : Color.BLACK;
        int resultColor = color.getRGB();
        return resultColor;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setRw(double rw) {
        this.rw = rw;
    }

    public void setGw(double gw) {
        this.gw = gw;
    }

    public void setBw(double bw) {
        this.bw = bw;
    }
}
