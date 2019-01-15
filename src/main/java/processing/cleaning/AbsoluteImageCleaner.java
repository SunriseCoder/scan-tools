package processing.cleaning;

import java.awt.image.BufferedImage;
import java.util.List;

import processing.cleaning.preset.Condition;
import processing.cleaning.preset.Preset;
import processing.cleaning.preset.Replacement;

public class AbsoluteImageCleaner {

    public void clean(BufferedImage image, Preset preset) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int actualRGB = image.getRGB(x, y);
                int actualRed = (actualRGB >> 16) & 0xFF;
                int actualGreen = (actualRGB >> 8) & 0xFF;
                int actualBlue = actualRGB & 0xFF;

                List<Replacement> operations = preset.getReplacements();
                for (Replacement operation : operations) {
                    Condition condition = operation.getCondition();
                    if (isMatches(condition, actualRed, actualGreen, actualBlue)) {
                        image.setRGB(x, y, operation.getReplacement().getRGB());
                    }
                }
            }
        }
    }

    private boolean isMatches(Condition condition, int actualRed, int actualGreen, int actualBlue) {
        boolean result = actualRed >= condition.getRed().getMin() && actualRed <= condition.getRed().getMax();
        result &= actualGreen >= condition.getGreen().getMin() && actualGreen <= condition.getGreen().getMax();
        result &= actualBlue >= condition.getBlue().getMin() && actualBlue <= condition.getBlue().getMax();

        return result;
    }
}
