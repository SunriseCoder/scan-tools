package core.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class YoutubeChannel {
    private String channelId;
    private String title;

    @JsonIgnore
    private Map<String, YoutubeVideo> videos;

    public YoutubeChannel() {
        this(null);
    }

    public YoutubeChannel(String channelId) {
        this.channelId = channelId;
        videos = new HashMap<>();
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public String getStatusString() {
        return videos.size() + " video(s)";
    }

    public void addVideo(YoutubeVideo video) {
        videos.put(video.getVideoId(), video);

    }

    public boolean containVideo(String videoId) {
        boolean result = videos.containsKey(videoId);
        return result;
    }

    @Override
    public String toString() {
        return channelId + ": " + title;
    }
}
