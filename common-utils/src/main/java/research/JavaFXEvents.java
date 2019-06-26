package research;

import javafx.application.Application;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFXEvents extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();

        ImageView imageView = new ImageView();
        imageView.addEventHandler(EventType.ROOT, e -> System.out.println(e));
        imageView.setOnKeyPressed(e -> System.out.println(e));
        imageView.setFocusTraversable(true);
        root.getChildren().add(imageView);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene );

        primaryStage.setTitle("JavaFX Events Test");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
