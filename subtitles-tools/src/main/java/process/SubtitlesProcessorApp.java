package process;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import process.context.ApplicationContext;
import utils.FileUtils;

public class SubtitlesProcessorApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "subtitles-processor-config.json";

    public static void main(String[] args) {
        launch(args);
    }

    private ApplicationContext applicationContext;

    @FXML
    private SplitPane splitPane;

    private MediaPlayer mediaPleer;

    private File currentMediaFile;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);

        // Root UI Node
        Parent root = FileUtils.loadFXML(this);

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Subtitles Processor");
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Restore sizes of Visual Components (Panes, Windows, etc)
        // TODO Fix for both SplitPanes
        //restoreComponent();
    }

    // TODO Fix for both SplitPanes
    /*private void restoreComponent() {
        // Restore SplitPane Dividers
        String positionsString = applicationContext.getParameterValue(ApplicationParameters.SplitPaneDivider);
        if (positionsString != null) {
            double[] positions = Arrays.stream(positionsString.split(";"))
                    .mapToDouble(s -> Double.parseDouble(s)).toArray();
            splitPane.setDividerPositions(positions);
        }

        // Listener to Save SplitPane Dividers on SplitPane Dividers change
        splitPane.getDividers().forEach(div -> {
            div.positionProperty().addListener(e -> {
                double[] dividerPositions = splitPane.getDividerPositions();
                String dividerPositionsString = Arrays.stream(dividerPositions).boxed()
                        .map(d -> String.valueOf(d))
                        .collect(Collectors.joining(";"));
                applicationContext.setParameterValue(ApplicationParameters.SplitPaneDivider, dividerPositionsString);
            });
        });

        // Restore Working Folder
        String startFolderPath = applicationContext.getParameterValue(ApplicationParameters.StartFolder);
        if (startFolderPath != null) {
            File startFolder = new File(startFolderPath);
            if (startFolder.exists() && startFolder.isDirectory()) {
                applicationContext.fireEvent(ApplicationEvents.WorkFolderChanged, startFolder);
            }
        }
    }*/

    @FXML
    private void selectMediaFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Folder with Images");
        ExtensionFilter filter = new ExtensionFilter("Wave files (*.wav)", "*.wav");
        fileChooser.getExtensionFilters().add(filter);

        if (currentMediaFile != null) {
            fileChooser.setInitialDirectory(currentMediaFile.getParentFile());
        }

        File newFolder = fileChooser.showOpenDialog(null);

        if (newFolder != null && newFolder.exists() && !newFolder.isDirectory()) {
            handleChangeMediaFile(newFolder);
        }
    }

    private void handleChangeMediaFile(File file) {
        currentMediaFile = file;
        Media media = new Media(file.toURI().toString());
        mediaPleer = new MediaPlayer(media);
    }

    @FXML
    private void handlePlay() {
        if (mediaPleer != null) {
            mediaPleer.play();
        }
    }

    @FXML
    private void handlePause() {
        if (mediaPleer != null) {
            mediaPleer.pause();
        }
    }
}
