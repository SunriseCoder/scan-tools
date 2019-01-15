package processing;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import processing.cleaning.AbsoluteImageCleaner;
import processing.cleaning.ColorPredicate;
import processing.cleaning.RelativeImageCleaner;
import processing.cleaning.preset.Condition;
import processing.cleaning.preset.Preset;
import processing.cleaning.preset.Range;
import processing.cleaning.preset.Replacement;

public class ProcessingApp {

    public static void main(String[] args) throws IOException {
        File sourceFolder = new File("data/in");
        File outputFolder = new File("data/out");

        Preset cleaningPreset = createPreset();
        List<ColorPredicate> predicates = createPredicates();
        File[] files = sourceFolder.listFiles();

        for (File file : files) {
            // Begin
            if (file.isDirectory()) {
                continue;
            }
            System.out.print("Processing " + file.getName() + "... ");
            long start = System.currentTimeMillis();

            // Loading image
            BufferedImage image = ImageIO.read(file);

            // Cropping
            image = image.getSubimage(0, 876, 5100, 5668);

            // Cleaning
            AbsoluteImageCleaner absoluteCleaner = new AbsoluteImageCleaner();
            //absoluteCleaner.clean(image, cleaningPreset);
            RelativeImageCleaner relativeCleaner = new RelativeImageCleaner();
            relativeCleaner.clean(image, predicates , Color.BLACK, Color.WHITE);

            // Saving image
            ImageIO.write(image, "PNG", new File(outputFolder, file.getName()));

            // End
            long end = System.currentTimeMillis();
            System.out.println(" done in " + (end - start) + "ms");
        }

        System.out.println("Done");
    }

    private static Preset createPreset() {
        Preset preset = new Preset("Remove pink lines");

        List<Replacement> replacements = new ArrayList<>();
        replacements.add(new Replacement("Replace pink objects",
                new Condition(new Range(187, 255), new Range(42, 193), new Range(79, 212)), Color.WHITE));
        replacements.add(new Replacement("Replace black lines",
                new Condition(new Range(0, 163), new Range(0, 153), new Range(0, 148)), Color.WHITE));

        preset.setReplacements(replacements);
        return preset;
    }

    private static List<ColorPredicate> createPredicates() {
        List<ColorPredicate> predicates = new ArrayList<>();

        predicates.add(rgb -> { // Keep blue lines
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            boolean result = (double) blue / (double) red > 1.4;
            result &= (double) blue / (double) green > 1.3;
            return result;
        });

        return predicates;
    }
}
