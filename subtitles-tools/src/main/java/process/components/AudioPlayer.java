package process.components;

import java.io.File;
import java.io.IOException;

import audio.wav.WaveInputStream;
import components.containers.CanvasPane;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.context.ApplicationParameters;
import utils.FileUtils;
import utils.MathUtils;

public class AudioPlayer {
    private static final int VERTICAL_SCALE = 16 * 1024;
    private static final int MIN_HORIZONTAL_SCALE = 32;
    private static final int MAX_HORIZONTAL_SCALE = 1024 * 1024;

    private ApplicationContext applicationContext;

    @FXML
    private Slider volumeSlider;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField openMediaFileTextField;
    private CanvasPane image;

    // Components
    private File currentMediaFile;
    private MediaPlayer mediaPleer;
    private ScaledSampleStorage sampleStorage;

    // Logic Parameters
    private long currentSamplePosition;
    private AudioPlayerSelection selection;

    // Visual Parameters
    private int scale;
    private int imageOffset;

    // Temporary variables
    private int mouseImagePosition;

    public AudioPlayer() {
        selection = new AudioPlayerSelection();
    }

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        applicationContext.addEventListener(ApplicationEvents.WorkMediaFileChanged,
                value -> handleStartMediaFileChanged(value));

        Parent node = FileUtils.loadFXML(this);

        node.setFocusTraversable(true);
        node.setOnMousePressed(e -> node.requestFocus());
        node.setOnKeyPressed(e -> handleKeyPressed(e));

        image = new CanvasPane();
        image.setMinHeight(200);
        GridPane.setColumnSpan(image, 12);
        GridPane.setVgrow(image, Priority.ALWAYS);
        gridPane.getChildren().add(image);

        image.widthProperty().addListener(e -> render());
        image.heightProperty().addListener(e -> render());

        image.setOnMousePressed(e -> handleImageMousePressed(e));
        image.setOnScroll(e -> handleImageScroll(e));
        image.setOnMouseDragged(e -> handleImageMouseDragged(e));

        return node;
    }

    public void restoreComponents() {
        String volumeString = applicationContext.getParameterValue(ApplicationParameters.AudioPlayerVolume);
        if (volumeString != null) {
            double volume = Double.parseDouble(volumeString);
            volumeSlider.setValue(volume);
        }

        volumeSlider.valueProperty().addListener(e -> {
            String volume = String.valueOf(volumeSlider.getValue());
            applicationContext.setParameterValue(ApplicationParameters.AudioPlayerVolume, volume);
        });
    }

    private void handleStartMediaFileChanged(Object value) {
        try {
            handleChangeMediaFile((File) value);
        } catch (Exception e) {
            applicationContext.showError("Could not open file", e);
        }
    }

    private void handleKeyPressed(KeyEvent e) {
         switch (e.getCode()) {
            case SPACE:
                if (mediaPleer != null) {
                    if (mediaPleer.getStatus().equals(Status.PLAYING)) {
                        handlePause();
                    } else {
                        mediaPleer.play();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleImageMousePressed(MouseEvent e) {
        mouseImagePosition = (int) e.getX();

        long samplePosition = getImageToSample((int) e.getX());
        if (e.getButton().equals(MouseButton.PRIMARY)) {
            setPlaybackPosition(samplePosition);
        } else if (e.getButton().equals(MouseButton.SECONDARY)) {
            setSelection(samplePosition, null);
        }

        render();
    }

    private void handleImageScroll(ScrollEvent e) {
        // Calculating Sample Position on the Image before Scale
        double sample = (e.getX() - imageOffset) * scale;

        // Calculating Scale
        scale = e.getDeltaY() < 0 ? scale * 2 : scale / 2;
        // Adjust Scale between the Boundaries
        scale = Math.max(scale, MIN_HORIZONTAL_SCALE);
        scale = Math.min(scale, MAX_HORIZONTAL_SCALE);

        // Calculating Sample Position on the Image after Scale
        int samplePositionOnImage = MathUtils.roundToInt(sample / scale + imageOffset);
        // Adjusting offset by Delta
        imageOffset += e.getX() - samplePositionOnImage;

        render();
    }

    private void handleImageMouseDragged(MouseEvent e) {
        if (e.getButton().equals(MouseButton.PRIMARY)) {
            int deltaX = (int) (e.getX() - mouseImagePosition);
            mouseImagePosition = (int) e.getX();
            imageOffset += deltaX;
        } else if (e.getButton().equals(MouseButton.SECONDARY)) {
            long start = getImageToSample(mouseImagePosition);
            long end = getImageToSample((int) e.getX());
            setSelection(start, end);
        }

        render();
    }

    private void setPlaybackPosition(long sample) {
        if (sample < 0 || sample >= sampleStorage.getFrameCount()) {
            return;
        }

        setSamplePosition(sample);

        setMediaPlayerPlaybackPosition();
    }

    private void setMediaPlayerPlaybackPosition() {
        long milliseconds = getSampleToPlayer(currentSamplePosition);
        mediaPleer.seek(new Duration(milliseconds));
    }

    private void render() {
        if (sampleStorage == null) {
            return;
        }

        image.clear();

        int[] samples = sampleStorage.getSamples(scale);

        int width = Math.min((int) image.getWidth(), samples.length + imageOffset);
        int height = (int) image.getHeight();

        // Wave Rendering
        GraphicsContext graphics = image.getGraphics();
        graphics.setStroke(Color.BLUE);
        for (int x = 0; x < width; x++) {
            int index = x - imageOffset;
            if (index < 0 || index >= samples.length) {
                continue;
            }

            int value = MathUtils.roundToInt((double) samples[index] * height / VERTICAL_SCALE);
            graphics.strokeLine(x, (height - value) / 2, x, (height + value) / 2);
        }

        // Selection rendering
        if (selection.isStartNotEmpty()) {
            long selectionStart = getSampleToImage(selection.getStart());
            long selectionEnd = selection.isEndEmpty() ? selectionStart : getSampleToImage(selection.getEnd());
            graphics.setFill(Color.GREEN);
            graphics.setGlobalAlpha(0.3);
            graphics.fillRect(selectionStart, 0, selectionEnd - selectionStart + 1, height);
            graphics.setGlobalAlpha(1);
        }

        // Cursor Rendering
        long cursorPosition = getSampleToImage(currentSamplePosition);
        if (cursorPosition >= 0 && cursorPosition < image.getWidth()) {
            graphics.setStroke(Color.RED);
            graphics.strokeLine(cursorPosition, 0, cursorPosition, height);
        }
    }

    private long getSampleToImage(long samplePosition) {
        return samplePosition / scale + imageOffset;
    }

    private long getImageToSample(long imagePosition) {
        return (imagePosition - imageOffset) * scale;
    }

    private long getSampleToPlayer(long samplePosition) {
        int sampleRate = MathUtils.roundToInt(sampleStorage.getAudioFormat().getSampleRate());
        int player = MathUtils.roundToInt(1000 * samplePosition / sampleRate);
        return player;
    }

    private long getPLayerToSample(long playerPosition) {
        int sampleRate = MathUtils.roundToInt(sampleStorage.getAudioFormat().getSampleRate());
        int sample = MathUtils.roundToInt(playerPosition * sampleRate / 1000);
        return sample;
    }

    private void calculateScale() {
        int width = (int) image.getWidth();
        long frameCount = sampleStorage.getFrameCount();
        double rate = frameCount / width;
        double power = MathUtils.ceilToInt(Math.log(rate) / Math.log(2));
        scale = MathUtils.roundToInt(Math.pow(2, power));
    }

    @FXML
    private void selectMediaFile() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Folder with Images");
        ExtensionFilter filter = new ExtensionFilter("Wave files (*.wav)", "*.wav");
        fileChooser.getExtensionFilters().add(filter);

        if (currentMediaFile != null) {
            fileChooser.setInitialDirectory(currentMediaFile.getParentFile());
        }

        File newFile = fileChooser.showOpenDialog(null);

        if (newFile != null && newFile.exists() && !newFile.isDirectory()) {
            handleChangeMediaFile(newFile);
        }
    }

    private void handleChangeMediaFile(File file) throws Exception {
        currentMediaFile = file;
        openMediaFileTextField.setText(file.getAbsolutePath());

        applicationContext.setParameterValue(ApplicationParameters.WorkMediaFile, file.getAbsolutePath());

        Media media = new Media(file.toURI().toString());
        mediaPleer = new MediaPlayer(media);
        mediaPleer.currentTimeProperty().addListener((e) -> handleMediaPlayerPlaybackPositionChanged(e));
        mediaPleer.volumeProperty().bind(volumeSlider.valueProperty());

        WaveInputStream inputStream = WaveInputStream.create(file, 0);
        sampleStorage = new ScaledSampleStorage(inputStream, MIN_HORIZONTAL_SCALE);
        calculateScale();
        render();
    }

    private void handleMediaPlayerPlaybackPositionChanged(Observable e) {
        if (mediaPleer.getStatus().equals(Status.PAUSED)) {
            return;
        }

        // Calculating Sample Position
        long playerPosition = MathUtils.roundToLong(mediaPleer.getCurrentTime().toMillis());
        long samplePosition = getPLayerToSample(playerPosition);

        // Adjusting that Sample Position don't come outside Selected Interval
        if (selection.isEndNotEmpty() && samplePosition > selection.getEnd()) {
            samplePosition = selection.getEnd();
            mediaPleer.pause();
        }

        if (!mediaPleer.getStatus().equals(Status.PLAYING)) {
            return;
        }

        // Firing Play Position Change Event
        playerPosition = getSampleToPlayer(samplePosition);
        applicationContext.fireEvent(ApplicationEvents.AudioPlayerOnPlay, playerPosition);

        // Calculating offset, that the Cursor don't go outside the Window
        long imagePosition = getSampleToImage(samplePosition);
        int imageWidth = (int) image.getWidth();
        if (imagePosition > 0.9 * imageWidth) {
            imageOffset -= imagePosition - 50;
        } else if (imagePosition < 50) {
            imageOffset += 50 - imagePosition;
        }

        setSamplePosition(samplePosition);
    }

    @FXML
    public void handlePlay() {
        if (mediaPleer != null) {
            if (selection.isStartNotEmpty() && selection.isEndNotEmpty()) {
                setPlaybackPosition(selection.getStart());
            }
            mediaPleer.play();
        }
    }

    @FXML
    public void handlePause() {
        if (mediaPleer != null) {
            long playerPosition = MathUtils.roundToLong(mediaPleer.getCurrentTime().toMillis());
            mediaPleer.pause();
            long samplePosition = getPLayerToSample(playerPosition);
            setSamplePosition(samplePosition);
        }
    }

    @FXML
    private void handleToSelectionStart() {
        if (selection.isStartNotEmpty()) {
            setPlaybackPosition(selection.getStart());
        }
    }

    @FXML
    private void handleToSelectionEnd() {
        if (selection.isEndNotEmpty()) {
            setPlaybackPosition(selection.getEnd());
        }
    }

    @FXML
    private void handleSetSelectionStart() {
        setSelection(currentSamplePosition, null);
        render();
    }

    @FXML
    private void handleSetSelectionEnd() {
        selection.setEnd(currentSamplePosition);
        render();
    }

    public AudioPlayerSelection getSelectionInMilliseconds() {
        if (selection.isStartEmpty() || selection.isEndEmpty() || sampleStorage == null) {
            return null;
        }

        long selectionStart = getSampleToPlayer(selection.getStart());
        long selectionEnd = getSampleToPlayer(selection.getEnd());
        AudioPlayerSelection selectionInMilliseconds = new AudioPlayerSelection(selectionStart, selectionEnd);
        return selectionInMilliseconds;
    }

    public void setSelectionInMilliseconds(AudioPlayerSelection selectionInMilliseconds) {
        if (sampleStorage == null) {
            return;
        }

        long selectionStart = getPLayerToSample(selectionInMilliseconds.getStart());
        long selectionEnd = getPLayerToSample(selectionInMilliseconds.getEnd());

        setSelection(selectionStart, selectionEnd);
    }

    private void setSamplePosition(long position) {
        currentSamplePosition = position;
        render();
    }

    private void setSelection(Long start, Long end) {
        // TODO Add checks that both boundaries of the selection are inside the Sample Area
        selection.setStart(start);
        selection.setEnd(end);
        render();
    }

    public void resetSelectionInterval() {
        selection.reset();
        render();
    }
}
