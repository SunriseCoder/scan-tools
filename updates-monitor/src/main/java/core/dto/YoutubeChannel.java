package core.dto;

import java.beans.Transient;

public class YoutubeChannel {
    private String channelId;
    private String title;

    public YoutubeChannel() {
        // Default constructor
    }

    public YoutubeChannel(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Transient
    public String getStatusString() {
        return channelId + ": " + (title == null ? "Unknown yet" : title);
    }
}
