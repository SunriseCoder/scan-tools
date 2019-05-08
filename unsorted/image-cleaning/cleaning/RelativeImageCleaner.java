package processing.cleaning;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class RelativeImageCleaner {

    public void clean(BufferedImage image, List<ColorPredicate> predicates, Color match, Color notMatch) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int actualRGB = image.getRGB(x, y);
                for (ColorPredicate predicate : predicates) {
                    Color replacement = predicate.test(actualRGB) ? match : notMatch;
                    image.setRGB(x, y, replacement.getRGB());
                }
            }
        }
    }
}
