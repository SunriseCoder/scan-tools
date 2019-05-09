package process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import process.components.AudioPlayer;
import process.components.AudioPlayer.SelectionInterval;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.context.ApplicationParameters;
import process.dto.SubtitleDTO;
import process.dto.SubtitleTimeDTO;
import utils.FileUtils;
import wrappers.IntWrapper;

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

    private File currentSubtitlesFile;

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

        subtitlesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        subtitlesListView.setCellFactory(c -> new SubtitleListCell());
        subtitlesListView.setOnKeyPressed(e -> handleSubtitlesListViewKeyPressed(e));
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
        subtitles.add(new SubtitleDTO(new SubtitleTimeDTO(0, 1, 7, 258), new SubtitleTimeDTO(0, 1, 9, 358),
                "Dude!\nHow are You?"));
        subtitles.add(new SubtitleDTO(new SubtitleTimeDTO(0, 1, 10, 315), new SubtitleTimeDTO(0, 1, 12, 798),
                "I missed You so much!"));

        ObservableList<SubtitleDTO> items = FXCollections.observableArrayList(subtitles);
        subtitlesListView.setItems(items);
    }

    // TODO Fix for both SplitPanes
    private void restoreComponent() {
        // Restore SplitPane Dividers
        /*
         * String positionsString =
         * applicationContext.getParameterValue(ApplicationParameters.SplitPaneDivider);
         * if (positionsString != null) { double[] positions =
         * Arrays.stream(positionsString.split(";")) .mapToDouble(s ->
         * Double.parseDouble(s)).toArray(); splitPane.setDividerPositions(positions); }
         *
         * // Listener to Save SplitPane Dividers on SplitPane Dividers change
         * splitPane.getDividers().forEach(div -> { div.positionProperty().addListener(e
         * -> { double[] dividerPositions = splitPane.getDividerPositions(); String
         * dividerPositionsString = Arrays.stream(dividerPositions).boxed() .map(d ->
         * String.valueOf(d)) .collect(Collectors.joining(";"));
         * applicationContext.setParameterValue(ApplicationParameters.SplitPaneDivider,
         * dividerPositionsString); }); });
         */

        // Restore Working Folder
        String startFolderPath = applicationContext.getParameterValue(ApplicationParameters.WorkMediaFile);
        if (startFolderPath != null) {
            File startFile = new File(startFolderPath);
            if (startFile.exists() && !startFile.isDirectory()) {
                applicationContext.fireEvent(ApplicationEvents.WorkMediaFileChanged, startFile);
            }
        }
    }

    private void handleSubtitlesListViewKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case DELETE:
                handleDeleteSubtitlesPressed();
                break;
            default:
                break;
        }
    }

    private static class SubtitleListCell extends ListCell<SubtitleDTO> {
        @Override
        protected void updateItem(SubtitleDTO item, boolean empty) {
            super.updateItem(item, empty);

            VBox node = null;
            if (!empty) {
                node = new VBox();
                node.getChildren().add(new Label(item.getTimeAsString()));
                node.getChildren().add(new Label(item.getText()));
            }
            setGraphic(node);
        }
    }

    @FXML
    private void handleAddSubtitlePressed() {
        SelectionInterval selectionInterval = audioPlayer.getSelectionInterval();
        String selectedText = textEditor.getSelectedText();

        if (selectionInterval == null || selectedText == null || selectedText.isEmpty()) {
            String message = "To Add new Subtitle You have to:\n"
                    + "1. Select Audio Interval on Wave Diagram via Mouse Left Click and Drag\n"
                    + "2. Type and Select Subtitle Text in Text Editor\n"
                    // TODO Add HotKey here
                    + "3. Press Add Button or ??? Hotkey";
            applicationContext.showError(message, null);
        }

        SubtitleTimeDTO start = new SubtitleTimeDTO(selectionInterval.getStart());
        SubtitleTimeDTO end = new SubtitleTimeDTO(selectionInterval.getEnd());
        SubtitleDTO subtitle = new SubtitleDTO(start, end, selectedText);

        subtitlesListView.getItems().add(subtitle);
        subtitlesListView.refresh();

        audioPlayer.resetSelectionInterval();
        textEditor.replaceSelection("");
    }

    @FXML
    private void handleEditSubtitlePressed() {
        SubtitleDTO subtitle = subtitlesListView.getSelectionModel().getSelectedItem();

        // Setting Selection Interval on AudioPlayer
        long start = subtitle.getStart().getAsMilliseconds();
        long end = subtitle.getEnd().getAsMilliseconds();
        SelectionInterval selectionInterval = new SelectionInterval(start , end);
        audioPlayer.setSelectionInterval(selectionInterval);

        // Setting Selected Text in TextEditor
        String text = textEditor.getText();
        if (!text.isEmpty()) {
            text = "\n\n" + text;
        }
        text = subtitle.getText() + text;
        textEditor.setText(text);
        textEditor.selectRange(0, subtitle.getText().length());

        // Removing Subtitle from the List
        subtitlesListView.getItems().remove(subtitle);
    }

    @FXML
    private void handleDeleteSubtitlesPressed() {
        ObservableList<SubtitleDTO> selectedItems = subtitlesListView.getSelectionModel().getSelectedItems();
        subtitlesListView.getItems().removeAll(selectedItems);
    }

    @FXML
    private void handleSortSubtitlesPressed() {
        subtitlesListView.getItems()
                .sort((a, b) -> (int) (a.getStart().getAsMilliseconds() - b.getStart().getAsMilliseconds()));
    }

    @FXML
    private void handleSubtitlesOpenFile() throws IOException {
        openSubtitlesFile();
        handleSubtitlesRefreshFromFile();
    }

    @FXML
    private void handleSubtitlesRefreshFromFile() throws IOException {
        if (currentSubtitlesFile == null) {
            applicationContext.showError("Select Subtitles File first", null);
            return;
        }

        subtitlesListView.getItems().clear();

        // Reading Subtitles from File
        List<SubtitleDTO> subtitles = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(currentSubtitlesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip Subtitle Number

                // Reading Subtitle Time Interval
                line = reader.readLine();
                SubtitleDTO subtitle = new SubtitleDTO();
                subtitle.parseTime(line);

                // Reading Subtitle Text
                String text = "";
                while (!(line = reader.readLine()).isEmpty()) {
                    text += line;
                }
                subtitle.setText(text);

                subtitles.add(subtitle);
            }
        }

        ObservableList<SubtitleDTO> items = FXCollections.observableArrayList(subtitles);
        subtitlesListView.setItems(items);
    }

    @FXML
    private void handleSubtitlesSaveToFile() throws IOException {
        if (currentSubtitlesFile == null) {
            saveSubtitlesFile();
        }

        if (currentSubtitlesFile == null) {
            return;
        }

        FileUtils.createFile(currentSubtitlesFile, true);
        try (PrintWriter writer = new PrintWriter(currentSubtitlesFile)) {
            IntWrapper counter = new IntWrapper(1);
            ObservableList<SubtitleDTO> subtitles = subtitlesListView.getItems();
            subtitles.forEach(subtitle -> {
                writer.println(counter.postIncrement());
                writer.println(subtitle.getTimeAsString());
                writer.println(subtitle.getText());
                writer.println();
            });
            writer.flush();
        }
    }

    private void openSubtitlesFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Subtitles File");
        ExtensionFilter filter = new ExtensionFilter("Subtitles files (*.srt)", "*.srt");
        fileChooser.getExtensionFilters().add(filter);

        if (currentSubtitlesFile != null) {
            fileChooser.setInitialDirectory(currentSubtitlesFile.getParentFile());
        }

        File newFile = fileChooser.showOpenDialog(null);

        if (newFile != null && newFile.exists() && !newFile.isDirectory()) {
            currentSubtitlesFile = newFile;
        }
    }

    private void saveSubtitlesFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Subtitles File");
        ExtensionFilter filter = new ExtensionFilter("Subtitles files (*.srt)", "*.srt");
        fileChooser.getExtensionFilters().add(filter);

        if (currentSubtitlesFile != null) {
            fileChooser.setInitialDirectory(currentSubtitlesFile.getParentFile());
        }

        File newFile = fileChooser.showSaveDialog(null);

        if (newFile != null && !newFile.isDirectory()) {
            currentSubtitlesFile = newFile;
        }
    }
}
