package process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import process.components.AudioPlayer;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.context.ApplicationParameters;
import process.dto.SubtitleDTO;
import process.dto.SubtitleTimeDTO;
import utils.FileUtils;

public class SubtitlesProcessorApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "subtitles-processor-config.json";

    public static void main(String[] args) {
        launch(args);
    }

    private ApplicationContext applicationContext;

    @FXML
    private SplitPane splitPane;

    @FXML
    private ListView<SubtitleDTO> subtitlesListView;

    @FXML
    private TextArea textEditor;
    @FXML
    private Line lineSizeLine;

    private AudioPlayer audioPlayer;

    public SubtitlesProcessorApp() {
        audioPlayer = new AudioPlayer();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);

        // Root UI Node
        Parent root = FileUtils.loadFXML(this);

        Node audioPlayerNode = audioPlayer.createUI(applicationContext);
        splitPane.getItems().add(0, audioPlayerNode);

        subtitlesListView.setCellFactory((c) -> new SubtitleListCell());
        // TODO Delete Stub
        initListView();

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Subtitles Processor");
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Restore sizes of Visual Components (Panes, Windows, etc)
        // TODO Fix for both SplitPanes
        restoreComponent();
    }

    private void initListView() {
        List<SubtitleDTO> subtitles = new ArrayList<>();

        subtitles.add(new SubtitleDTO(new SubtitleTimeDTO(0, 1, 5, 15), new SubtitleTimeDTO(0, 1, 6, 358), "Hello!"));
        subtitles.add(new SubtitleDTO(new SubtitleTimeDTO(0, 1, 7, 258), new SubtitleTimeDTO(0, 1, 9, 358), "Dude!\nHow are You?"));
        subtitles.add(new SubtitleDTO(new SubtitleTimeDTO(0, 1, 10, 315), new SubtitleTimeDTO(0, 1, 12, 798), "I missed You so much!"));

        ObservableList<SubtitleDTO> items = FXCollections.observableArrayList(subtitles);
        subtitlesListView.setItems(items );
    }

    // TODO Fix for both SplitPanes
    private void restoreComponent() {
        // Restore SplitPane Dividers
        /*String positionsString = applicationContext.getParameterValue(ApplicationParameters.SplitPaneDivider);
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
        });*/

        // Restore Working Folder
        String startFolderPath = applicationContext.getParameterValue(ApplicationParameters.WorkMediaFile);
        if (startFolderPath != null) {
            File startFile = new File(startFolderPath);
            if (startFile.exists() && !startFile.isDirectory()) {
                applicationContext.fireEvent(ApplicationEvents.WorkMediaFileChanged, startFile);
            }
        }
    }

    private static class SubtitleListCell extends ListCell<SubtitleDTO> {
        @Override
        protected void updateItem(SubtitleDTO item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                return;
            }

            VBox node = new VBox();
            node.getChildren().add(new Label(item.getTimeAsString()));
            node.getChildren().add(new Label(item.getText()));
            setGraphic(node);
        }
    }
}
