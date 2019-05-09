package audio.api;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

public interface FrameInputStream {
    boolean available() throws IOException;
    boolean available(int channel) throws IOException;
    AudioFormat getFormat();
    int readFrames(int[] frames) throws IOException;
    int readFrames(int channel, int[] frames) throws IOException;
    long getFramesCount();
    void close() throws IOException;
}
