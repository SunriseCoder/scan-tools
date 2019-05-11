package process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import process.components.AudioPlayer;
import process.components.AudioPlayerSelection;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.context.ApplicationParameters;
import process.dto.SubtitleDTO;
import process.dto.SubtitleTimeDTO;
import utils.FileUtils;
import utils.StringUtils;
import wrappers.IntWrapper;
import wrappers.LongWrapper;

public class SubtitlesProcessorApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "subtitles-processor-config.json";

    public static void main(String[] args) {
        launch(args);
    }

    private ApplicationContext applicationContext;

    // General
    @FXML
    private SplitPane verticalSplitPane;
    @FXML
    private SplitPane horizontalSplitPane;

    // TextEditor
    @FXML
    private TextArea textEditor;
    @FXML
    private Line lineSizeLine;

    // Subtitles
    @FXML
    private Spinner<Integer> fontSizeSpinner;
    @FXML
    private TextField subtitlesSearchTextField;
    @FXML
    private ListView<SubtitleDTO> subtitlesListView;
    @FXML
    private TextField subtitlesWorkFileTextField;

    private AudioPlayer audioPlayer;

    private File workSubtitlesFile;

    public SubtitlesProcessorApp() {
        audioPlayer = new AudioPlayer();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);

        applicationContext.addEventListener(ApplicationEvents.AudioPlayerOnPlay,
                value -> handleAudioPlayerPlayEvent(value));

        // Root UI Node
        Parent root = FileUtils.loadFXML(this);

        // AudioPlayer
        Node audioPlayerNode = audioPlayer.createUI(applicationContext);
        verticalSplitPane.getItems().add(0, audioPlayerNode);

        // TextEditor
        textEditor.setOnKeyPressed(e -> handleTextEditorKeyPressed(e));

        // Subtitles Text Size Spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 96, 12);
        fontSizeSpinner.setValueFactory(valueFactory);
        fontSizeSpinner.valueProperty().addListener(e -> handleFontSizeChanged());

        // Subtitles Search
        subtitlesSearchTextField.textProperty().addListener(e -> handleSubtitlesSerchTextChanged());

        // Subtitles ListView
        subtitlesListView.setOnMouseClicked(e -> handleSubtitlesMouseClicked(e));
        subtitlesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        subtitlesListView.setCellFactory(c -> new SubtitleListCell());
        subtitlesListView.setOnKeyPressed(e -> handleSubtitlesListViewKeyPressed(e));

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Subtitles Processor");
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Restore sizes of Visual Components (Panes, Windows, etc)
        restoreComponent();
    }

    private void handleAudioPlayerPlayEvent(Object value) {
        long position = (long) value;

        subtitlesListView.getSelectionModel().clearSelection();

        ObservableList<SubtitleDTO> items = subtitlesListView.getItems();
        List<SubtitleDTO> itemList = new ArrayList<>(items);
        SubtitleDTO subtitle = findSubtitleByPosition(itemList, position);

        if (subtitle != null) {
            subtitlesListView.getSelectionModel().select(subtitle);
            int selectedIndex = subtitlesListView.getSelectionModel().getSelectedIndex();
            selectedIndex = Math.max(0, selectedIndex - 3);
            subtitlesListView.scrollTo(selectedIndex);
        }
    }

    private SubtitleDTO findSubtitleByPosition(List<SubtitleDTO> items, long position) {
        if (items.size() == 0) {
            return null;
        } else if (items.size() == 1) {
            SubtitleDTO item = items.get(0);
            boolean match = item.getStart().getAsMilliseconds() <= position
                    && item.getEnd().getAsMilliseconds() >= position;
            return match ? item : null;
        } else {
            int size = items.size();
            int halfSize = size / 2;

            SubtitleDTO item = items.get(halfSize);

            List<SubtitleDTO> nextList = item.getStart().getAsMilliseconds() > position ? items.subList(0, halfSize)
                    : items.subList(halfSize, size);

            SubtitleDTO result = findSubtitleByPosition(nextList, position);
            return result;
        }
    }

    private void restoreComponent() throws IOException {
        // SplitPane Dividers
        restoreSplitPane(verticalSplitPane, ApplicationParameters.VerticalSplitPaneDivider);
        restoreSplitPane(horizontalSplitPane, ApplicationParameters.HorizontalSplitPaneDivider);

        // AudioPlayer Components
        audioPlayer.restoreComponents();

        // Work Folders
        String workMediaFilePath = applicationContext.getParameterValue(ApplicationParameters.MediaWorkFile);
        if (workMediaFilePath != null) {
            File workMediaFile = new File(workMediaFilePath);
            if (workMediaFile.exists() && !workMediaFile.isDirectory()) {
                applicationContext.fireEvent(ApplicationEvents.WorkMediaFileChanged, workMediaFile);
            }
        }

        // TextEditor Text
        String textEditorText = applicationContext.getParameterValue(ApplicationParameters.TextEditorText);
        if (textEditorText != null) {
            textEditor.setText(textEditorText);
        }

        // Font Size
        String fontSizeString = applicationContext.getParameterValue(ApplicationParameters.FontSize);
        if (fontSizeString != null) {
            fontSizeSpinner.getValueFactory().setValue(Integer.parseInt(fontSizeString));
        }

        // Subtitles File
        String workSubtitlesFilePath = applicationContext.getParameterValue(ApplicationParameters.SubtitlesWorkFile);
        if (workSubtitlesFilePath != null) {
            File workSubtitlesFile = new File(workSubtitlesFilePath);
            if (workSubtitlesFile.exists() && !workSubtitlesFile.isDirectory()) {
                setWorkSubtitlesFile(workSubtitlesFile);
                loadSubtitlesFromFile();
            }
        }
    }

    private void restoreSplitPane(SplitPane splitPane, ApplicationParameters applicationParameter) {
        // Restore SplitPane Dividers
        String positionsString = applicationContext.getParameterValue(applicationParameter);
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
                        .map(d -> String.valueOf(d)).collect(Collectors.joining(";"));
                applicationContext.setParameterValue(applicationParameter, dividerPositionsString);
            });
        });
    }

    private void handleTextEditorKeyPressed(KeyEvent e) {
        if (!e.isControlDown()) {
            return;
        }

        switch (e.getCode()) {
            // CTRL+S -> Save
            case S:
                saveTextEditorText();
                e.consume();
                break;

            // CTRL+Up -> Switch to AudioPlayer
            case UP:
                audioPlayer.requestFocus();
                break;

            // CTRL+Right -> Add Subtitle
            case RIGHT:
                handleAddSubtitlePressed();
                break;
            default:
                break;
        }
    }

    private void handleFontSizeChanged() {
        Font font = new Font("Liberation Mono", fontSizeSpinner.getValue());
        textEditor.setFont(font);

        applicationContext.setParameterValue(ApplicationParameters.FontSize, fontSizeSpinner.getValue().toString());
        subtitlesListView.refresh();
    }

    private void handleSubtitlesSerchTextChanged() {
        subtitlesListView.getSelectionModel().clearSelection();

        String searchText = subtitlesSearchTextField.getText();
        if (searchText.length() < 1) {
            return;
        }

        subtitlesListView.getItems().forEach(subtitle -> {
            if (subtitle.getText().contains(searchText)) {
                subtitlesListView.getSelectionModel().select(subtitle);
            }
        });
    }

    private void handleSubtitlesMouseClicked(MouseEvent e) {
        // Double Click via Left Mouse Button
        if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
            SubtitleDTO subtitle = subtitlesListView.getSelectionModel().getSelectedItem();
            setAudioPlayerSelection(subtitle);
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

    private class SubtitleListCell extends ListCell<SubtitleDTO> {
        @Override
        protected void updateItem(SubtitleDTO item, boolean empty) {
            super.updateItem(item, empty);

            VBox node = null;
            if (!empty) {
                node = new VBox();

                Integer fontSize = fontSizeSpinner.getValue();
                Font font = new Font(fontSize);

                Label timeLabel = new Label(item.getTimeAsString());
                timeLabel.setFont(font);
                timeLabel.setTextFill(Color.BLUE);
                node.getChildren().add(timeLabel);

                Label textLabel = new Label(item.getText());
                textLabel.setFont(font);
                textLabel.setTextFill(Color.BLACK);
                node.getChildren().add(textLabel);
            }
            setGraphic(node);
        }
    }

    @FXML
    private void handleAddSubtitlePressed() {
        AudioPlayerSelection selection = audioPlayer.getSelectionInMilliseconds();
        String selectedText = textEditor.getSelectedText();

        // Validating AudioPlayer and TextEditor Selections
        if (selection.isStartEmpty() || selection.isEndEmpty() || selectedText == null || selectedText.isEmpty()) {
            String message = "To Add new Subtitle You have to:\n"
                    + "1. Select Audio Interval on Wave Diagram via Mouse Left Click and Drag\n"
                    + "2. Type and Select Subtitle Text in Text Editor\n"
                    + "3. Press Add Button or Hotkey (CRTL+Right)";
            applicationContext.showError(message, null);
        }

        // Creating new Subtitle
        SubtitleTimeDTO start = new SubtitleTimeDTO(selection.getStart());
        SubtitleTimeDTO end = new SubtitleTimeDTO(selection.getEnd());
        String text = StringUtils.trimEndSymbols(selectedText, "\n");
        SubtitleDTO subtitle = new SubtitleDTO(start, end, text);

        // Adding to the List and Sorting the List
        subtitlesListView.getItems().add(subtitle);
        sortSubtitles();

        // Saving Subtitles File
        saveSubtitlesToFile();

        // Resetting Selections
        audioPlayer.resetSelectionInterval();
        textEditor.replaceSelection("");

        // Saving TextEditor Text
        saveTextEditorText();

        audioPlayer.requestFocus();
    }

    @FXML
    private void handleEditSubtitlePressed() {
        ObservableList<SubtitleDTO> subtitleItems = subtitlesListView.getSelectionModel().getSelectedItems();

        // User Confirmation about going to Edit Subtitle(s)
        boolean confirmed;
        if (subtitleItems.size() == 1) {
            confirmed = applicationContext.showConfirmation("Edit Subtitles", "Are You sure to Edit One Subtitle?");
        } else if (subtitleItems.size() > 1) {
            confirmed = applicationContext.showConfirmation("Edit Subtitles",
                    "You Are going to Edit Several Subtitles.\n"
                            + "This will lead to lose all Timings of these Subtitles.\nAre You sure?");
        } else {
            return;
        }
        if (!confirmed) {
            return;
        }

        // Deleting Selected Text from TextEditor
        String oldText = textEditor.getText();
        StringBuilder stringBuilder = new StringBuilder();
        List<SubtitleDTO> subtitles = new ArrayList<>(subtitleItems);
        subtitles.forEach(subtitle -> {
            // Adding Selected Text in TextEditor
            stringBuilder.append(subtitle.getText());
            stringBuilder.append("\n\n");

            // Removing Subtitle from the List
            subtitlesListView.getItems().remove(subtitle);
        });

        int subtitleTextLenght = stringBuilder.length();

        if (!oldText.isEmpty()) {
            stringBuilder.append("\n\n");
        }
        stringBuilder.append(oldText);

        String newText = stringBuilder.toString();
        textEditor.setText(newText);
        textEditor.selectRange(0, subtitleTextLenght);
        saveTextEditorText();

        // Setting Selection Interval on AudioPlayer
        if (subtitles.size() == 1) {
            SubtitleDTO subtitle = subtitles.get(0);
            setAudioPlayerSelection(subtitle);
        }
    }

    @FXML
    private void handleDeleteSubtitlesPressed() {
        boolean confirmed = applicationContext.showConfirmation("Delete Subtitle", "Are You sure to delete Subtitle?");
        if (confirmed) {
            ObservableList<SubtitleDTO> selectedItems = subtitlesListView.getSelectionModel().getSelectedItems();
            subtitlesListView.getItems().removeAll(selectedItems);
            saveSubtitlesToFile();
        }
    }

    @FXML
    private void handleSortSubtitlesPressed() {
        sortSubtitles();
    }

    private void sortSubtitles() {
        subtitlesListView.getItems()
                .sort((a, b) -> (int) (a.getStart().getAsMilliseconds() - b.getStart().getAsMilliseconds()));

        // Adjust that Subtitles does not Intersect with each others
        LongWrapper lastEnd = new LongWrapper(-1);
        subtitlesListView.getItems().forEach(subtitle -> {
            // Checking that Last End is Before Next Start
            if (subtitle.getStart().getAsMilliseconds() <= lastEnd.getValue()) {
                subtitle.setStart(new SubtitleTimeDTO(lastEnd.getValue() + 1));
            }

            // Checking that End is After Start
            if (subtitle.getEnd().getAsMilliseconds() <= subtitle.getStart().getAsMilliseconds()) {
                subtitle.setEnd(new SubtitleTimeDTO(subtitle.getStart().getAsMilliseconds() + 1));
            }

            lastEnd.setValue(subtitle.getEnd().getAsMilliseconds());
        });
        handleFontSizeChanged();
    }

    @FXML
    private void handleSubtitlesOpenFile() throws IOException {
        chooseSubtitlesFileToOpen();
        loadSubtitlesFromFile();
    }

    @FXML
    private void handleSubtitlesRefreshFromFile() throws IOException {
        loadSubtitlesFromFile();
    }

    private void chooseSubtitlesFileToOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Subtitles File");
        ExtensionFilter filter = new ExtensionFilter("Subtitles files (*.srt)", "*.srt");
        fileChooser.getExtensionFilters().add(filter);

        if (workSubtitlesFile != null) {
            fileChooser.setInitialDirectory(workSubtitlesFile.getParentFile());
        }

        File newFile = fileChooser.showOpenDialog(null);

        if (newFile != null && newFile.exists() && !newFile.isDirectory()) {
            setWorkSubtitlesFile(newFile);
        }
    }

    private void loadSubtitlesFromFile() throws IOException, FileNotFoundException {
        if (workSubtitlesFile == null) {
            applicationContext.showError("Select Subtitles File first", null);
            return;
        }

        subtitlesListView.getItems().clear();

        // Reading Subtitles from File
        List<SubtitleDTO> subtitles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(workSubtitlesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip Subtitle Number

                // Reading Subtitle Time Interval
                line = reader.readLine();
                SubtitleDTO subtitle = new SubtitleDTO();
                subtitle.parseTime(line);

                // Reading Subtitle Text
                String text = "";
                boolean firstLine = true;
                while (!(line = reader.readLine()).isEmpty()) {
                    if (firstLine) {
                        firstLine = false;
                    } else {
                        text += "\n";
                    }
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
        saveSubtitlesToFile();
    }

    private void saveSubtitlesToFile() {
        if (workSubtitlesFile == null) {
            chooseSubtitlesFileToSave();
        }

        if (workSubtitlesFile == null) {
            return;
        }

        try {
            doSubtitlesSaveToFile();
        } catch (IOException e) {
            applicationContext.showError("Could not Save Subtitles to File: " + workSubtitlesFile.getAbsolutePath(), e);
        }
    }

    private void doSubtitlesSaveToFile() throws IOException, FileNotFoundException {
        FileUtils.createFile(workSubtitlesFile, true);
        try (PrintWriter writer = new PrintWriter(workSubtitlesFile)) {
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

    private void chooseSubtitlesFileToSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Subtitles File");
        ExtensionFilter filter = new ExtensionFilter("Subtitles files (*.srt)", "*.srt");
        fileChooser.getExtensionFilters().add(filter);

        if (workSubtitlesFile != null) {
            fileChooser.setInitialDirectory(workSubtitlesFile.getParentFile());
        }

        File newFile = fileChooser.showSaveDialog(null);

        if (newFile != null && !newFile.isDirectory()) {
            setWorkSubtitlesFile(newFile);
        }
    }

    private void setWorkSubtitlesFile(File newFile) {
        workSubtitlesFile = newFile;

        String filePath = newFile.getAbsolutePath();
        applicationContext.setParameterValue(ApplicationParameters.SubtitlesWorkFile, filePath);

        subtitlesWorkFileTextField.setText(filePath);
    }

    private void saveTextEditorText() {
        String text = textEditor.getText();
        applicationContext.setParameterValue(ApplicationParameters.TextEditorText, text);
    }

    private void setAudioPlayerSelection(SubtitleDTO subtitle) {
        long start = subtitle.getStart().getAsMilliseconds();
        long end = subtitle.getEnd().getAsMilliseconds();
        AudioPlayerSelection selection = new AudioPlayerSelection(start, end);
        audioPlayer.setSelectionInMilliseconds(selection);
    }
}
