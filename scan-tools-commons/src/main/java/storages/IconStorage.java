package storages;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class IconStorage {
    public enum Icons {
        CHECKED_16("checked-16.png"),
        TRANSPARENT_16("transparent-16.png");

        private String filename;

        Icons(String filename) {
            this.filename = filename;
        }
    }

    private static Map<String, Image> images = new HashMap<>();

    public static Image getIcon(Icons icon) {
        Image image = images.get(icon.name());

        if (image != null) {
            return image;
        }

        try {
            image = loadImage(icon.filename);
            images.put(icon.name(), image);
        } catch (Exception e) {
            System.out.println("Error due to load icon image from file: " + icon.filename);
            e.printStackTrace();
        }

        return image;
    }

    private static Image loadImage(String filename) throws URISyntaxException {
        URL resource = IconStorage.class.getResource("/icons/" + filename);
        Image image = new Image(resource.toURI().toString());
        return image;
    }
}
