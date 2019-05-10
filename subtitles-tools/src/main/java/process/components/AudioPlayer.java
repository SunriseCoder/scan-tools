package process.components;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

import audio.api.FrameInputStream;
import audio.wav.WaveInputStream;
import components.containers.CanvasPane;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
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
import wrappers.IntArrayWrapper;

public class AudioPlayer {
    private static final int VERTICAL_SCALE = 16 * 1024;
    private static final int MIN_HORIZONTAL_SCALE = 32;
    private static final int MAX_HORIZONTAL_SCALE = 1024 * 1024;

    private ApplicationContext applicationContext;

    private CanvasPane image;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField openMediaFileTextField;

    private MediaPlayer mediaPleer;
    private File currentMediaFile;
    private ScaledSampleStorage sampleStorage;

    private int scale;
    private int mousePositionOnImage;
    private long samplePosition;
    private int offsetOnImage;

    private SelectionInterval selectionInterval;

    public AudioPlayer() {
        selectionInterval = new SelectionInterval();
    }

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        applicationContext.addEventListener(ApplicationEvents.WorkMediaFileChanged, value -> handleStartMediaFileChanged(value));

        Parent node = FileUtils.loadFXML(this);

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

    private void handleStartMediaFileChanged(Object value) {
        try {
            handleChangeMediaFile((File) value);
        } catch (Exception e) {
            applicationContext.showError("Could not open file", e);
        }
    }

    private void handleImageMousePressed(MouseEvent e) {
        mousePositionOnImage = (int) e.getX();

        long samplePosition = getSamplePosition((int) e.getX());
        if (e.getButton().equals(MouseButton.PRIMARY)) {
            setPlaybackPosition(samplePosition);
        } else if (e.getButton().equals(MouseButton.SECONDARY)) {
            setSelectionInterval(samplePosition, 0L);
        }

        render();
    }

    private void handleImageScroll(ScrollEvent e) {
        // Calculating Sample Position on the Image before Scale
        double sample = (e.getX() - offsetOnImage) * scale;

        // Calculating Scale
        scale = e.getDeltaY() < 0 ? scale * 2 : scale / 2;
        // Adjust Scale between the Boundaries
        scale = Math.max(scale, MIN_HORIZONTAL_SCALE);
        scale = Math.min(scale, MAX_HORIZONTAL_SCALE);

        // Calculating Sample Position on the Image after Scale
        int samplePositionOnImage = MathUtils.roundToInt(sample / scale + offsetOnImage);
        // Adjusting offset by Delta
        offsetOnImage += e.getX() - samplePositionOnImage;

        render();
    }

    private void handleImageMouseDragged(MouseEvent e) {
        if (e.getButton().equals(MouseButton.PRIMARY)) {
            int deltaX = (int) (e.getX() - mousePositionOnImage);
            mousePositionOnImage = (int) e.getX();
            offsetOnImage += deltaX;
        } else if (e.getButton().equals(MouseButton.SECONDARY)) {
            long start = getSamplePosition(mousePositionOnImage);
            long end = getSamplePosition((int) e.getX());
            setSelectionInterval(start, end);
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
        int milliseconds = getPositionForMediaPlayer(samplePosition);
        mediaPleer.seek(new Duration(milliseconds));
    }

    private void render() {
        if (sampleStorage == null) {
            return;
        }

        image.clear();

        int[] samples = sampleStorage.getSamples(scale);

        int width = Math.min((int) image.getWidth(), samples.length + offsetOnImage);
        int height = (int) image.getHeight();

        // Wave Rendering
        GraphicsContext graphics = image.getGraphics();
        graphics.setStroke(Color.BLUE);
        for (int x = 0; x < width; x++) {
            int index = x - offsetOnImage;
            if (index < 0 || index >= samples.length) {
                continue;
            }

            int value = MathUtils.roundToInt((double) samples[index] * height / VERTICAL_SCALE);
            graphics.strokeLine(x, (height - value) / 2, x, (height + value) / 2);
        }

        // Selected Interval rendering
        if (selectionInterval.start != null) {
            int start = getPositionOnImage(selectionInterval.start);
            int end = selectionInterval.end == null ? start : getPositionOnImage(selectionInterval.end);
            graphics.setFill(Color.GREEN);
            graphics.setGlobalAlpha(0.3);
            graphics.fillRect(start, 0, end - start + 1, height);
            graphics.setGlobalAlpha(1);
        }

        // Cursor Rendering
        int cursorPosition = getPositionOnImage(samplePosition);
        if (cursorPosition > 0 && cursorPosition < image.getWidth()) {
            graphics.setStroke(Color.RED);
            graphics.strokeLine(cursorPosition, 0, cursorPosition, height);
        }
    }

    private int getPositionOnImage(long samplePosition) {
        return (int) (samplePosition / scale + offsetOnImage);
    }

    private int getSamplePosition(int positionOnImage) {
        return (positionOnImage - offsetOnImage) * scale;
    }

    private int getPositionForMediaPlayer(long samplePosition) {
        int sampleRate = MathUtils.roundToInt(sampleStorage.getAudioFormat().getSampleRate());
        int milliseconds = MathUtils.roundToInt(1000 * samplePosition / sampleRate);
        return milliseconds;
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

        WaveInputStream inputStream = WaveInputStream.create(file, 0);
        sampleStorage = new ScaledSampleStorage(inputStream, MIN_HORIZONTAL_SCALE);
        calculateScale();
        render();
    }

    private void handleMediaPlayerPlaybackPositionChanged(Observable e) {
        // Calculating Sample Position
        double milliseconds = mediaPleer.getCurrentTime().toMillis();
        int sampleRate = MathUtils.roundToInt(sampleStorage.getAudioFormat().getSampleRate());
        long newSamplePosition = MathUtils.roundToLong(milliseconds * sampleRate / 1000);

        // Adjusting that Sample Position don't come outside Selected Interval
        if (selectionInterval.end != null && newSamplePosition > selectionInterval.end) {
            newSamplePosition = selectionInterval.end;
            mediaPleer.pause();
        }

        if (!mediaPleer.getStatus().equals(Status.PLAYING)) {
            return;
        }

        // Firing Play Position Change Event
        long samplePositionInMilliseconds = 1000 * newSamplePosition / sampleRate;
        applicationContext.fireEvent(ApplicationEvents.AudioPlayerOnPlay, samplePositionInMilliseconds);

        // Calculating offset, that the Cursor don't go outside the Window
        int positionOnImage = getPositionOnImage(newSamplePosition);
        int imageWidth = (int) image.getWidth();
        if (positionOnImage > 0.9 * imageWidth) {
            offsetOnImage -= positionOnImage - 50;
        } else if (positionOnImage < 50) {
            offsetOnImage += 50 - positionOnImage;
        }

        setSamplePosition(newSamplePosition);
    }

    @FXML
    public void handlePlay() {
        if (mediaPleer != null) {
            if (selectionInterval.start != null && selectionInterval.end != null) {
                setPlaybackPosition(selectionInterval.start);
            }
            mediaPleer.play();
        }
    }

    @FXML
    public void handlePause() {
        if (mediaPleer != null) {
            mediaPleer.pause();
        }
    }

    @FXML
    private void handleToSelectionStart() {
        if (selectionInterval.start != null) {
            setPlaybackPosition(selectionInterval.start);
        }
    }

    @FXML
    private void handleToSelectionEnd() {
        if (selectionInterval.end != null) {
            setPlaybackPosition(selectionInterval.end);
        }
    }

    @FXML
    private void handleSetSelectionStart() {
        setSelectionInterval(samplePosition, null);
        render();
    }

    @FXML
    private void handleSetSelectionEnd() {
        selectionInterval.end = samplePosition;
        render();
    }

    private static class ScaledSampleStorage {
        private Map<Integer, IntArrayWrapper> storage;
        private AudioFormat audioFormat;
        private long frameCount;

        public ScaledSampleStorage(FrameInputStream inputStream, int minimalScale) throws IOException {
            storage = new HashMap<>();
            audioFormat = inputStream.getFormat();
            frameCount = inputStream.getFramesCount();
            init(inputStream, minimalScale);
        }

        private void init(FrameInputStream inputStream, int minimalScale) throws IOException {
            long framesCount = inputStream.getFramesCount();
            int arraySize = MathUtils.ceilToInt((double) framesCount / minimalScale);
            IntArrayWrapper arrayWrapper = new IntArrayWrapper(arraySize);
            int[] array = arrayWrapper.getArray();

            int[] buffer = new int[minimalScale];
            for (int outer = 0; outer < arraySize; outer++) {
                int value = 0;
                int read = inputStream.readFrames(buffer);
                for (int inner = 0; inner < read; inner++) {
                    value += Math.abs(buffer[inner]);
                }
                value = MathUtils.roundToInt(value / read);
                array[outer] = value;
            }

            storage.put(minimalScale, arrayWrapper);
        }

        public AudioFormat getAudioFormat() {
            return audioFormat;
        }

        public long getFrameCount() {
            return frameCount;
        }

        public int[] getSamples(int scale) {
            IntArrayWrapper wrapper = getWrapper(scale);
            return wrapper.getArray();
        }

        private IntArrayWrapper getWrapper(int scale) {
            IntArrayWrapper wrapper = storage.get(scale);
            if (wrapper == null) {
                createWrapper(scale);
                wrapper = storage.get(scale);
            }

            return wrapper;
        }

        private IntArrayWrapper createWrapper(int scale) {
            IntArrayWrapper sourceWrapper = storage.get(scale / 2);
            if (sourceWrapper == null) {
                sourceWrapper = createWrapper(scale / 2);
            }

            int size = MathUtils.ceilToInt((double) sourceWrapper.getLength() / 2);
            IntArrayWrapper wrapper = new IntArrayWrapper(size);
            int[] sourceArray = sourceWrapper.getArray();
            int[] array = wrapper.getArray();
            for (int i = 0; i < array.length; i++) {
                int value = sourceArray[i * 2];
                double counter = 1;
                if (sourceArray.length > i * 2 + 1) {
                    value += sourceArray[i * 2 + 1];
                    counter++;
                }
                array[i] = MathUtils.roundToInt((double) value / counter);
            }

            storage.put(scale, wrapper);
            return wrapper;
        }
    }

    public static class SelectionInterval {
        private Long start;
        private Long end;

        public SelectionInterval() {
            // Default Constructor
        }

        public SelectionInterval(Long start, Long end) {
            this.start = start;
            this.end = end;
        }

        public Long getStart() {
            return start;
        }

        public Long getEnd() {
            return end;
        }
    }

    public SelectionInterval getSelectionIntervalInMilliseconds() {
        if (selectionInterval.start == null || selectionInterval.end == null || sampleStorage == null) {
            return null;
        }

        int sampleRate = MathUtils.roundToInt(sampleStorage.getAudioFormat().getSampleRate());
        long start = 1000 * selectionInterval.start / sampleRate;
        long end = 1000 * selectionInterval.end / sampleRate;
        SelectionInterval inMilliseconds = new SelectionInterval(start, end);
        return inMilliseconds;
    }

    public void setSelectionIntervalInMilliseconds(SelectionInterval inMilliseconds) {
        if (sampleStorage == null) {
            return;
        }

        int sampleRate = MathUtils.roundToInt(sampleStorage.getAudioFormat().getSampleRate());
        long start = inMilliseconds.start * sampleRate / 1000;
        long end = inMilliseconds.end * sampleRate / 1000;

        setSelectionInterval(start, end);
    }

    private void setSamplePosition(long position) {
        samplePosition = position;
        render();
    }

    private void setSelectionInterval(Long start, Long end) {
        // TODO Add checks that both boundaries of interval are in inside the Samples Area
        selectionInterval.start = start;
        selectionInterval.end = end;
        render();
    }

    public void resetSelectionInterval() {
        selectionInterval.start = null;
        selectionInterval.end = null;
        render();
    }
}
