package process.handlers;

import processing.images.filters.AbstractImageFilter;
import processing.images.filters.BilinearFilter;
import processing.images.filters.RoughFilter;

public enum SmoothFilters {
    BilinearFilter("Bilinear Filter", BilinearFilter.class),
    RoughFilter("Rough Filter", RoughFilter.class);

    private String text;
    Class<? extends AbstractImageFilter> cl;

    private SmoothFilters(String text, Class<? extends AbstractImageFilter> cl) {
        this.text = text;
        this.cl = cl;
    }

    public String getText() {
        return text;
    }

    public Class<? extends AbstractImageFilter> getCl() {
        return cl;
    }
}
