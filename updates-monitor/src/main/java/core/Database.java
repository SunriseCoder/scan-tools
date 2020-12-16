package core;

import java.util.HashMap;
import java.util.Map;

import core.dto.YoutubeChannel;
import core.dto.YoutubeVideo;

public class Database {
    private Map<String, YoutubeChannel> youtubeChannels;
    private Map<String, YoutubeVideo> youtubeVideos;

    public Database() {
        youtubeChannels = new HashMap<>();
        youtubeVideos = new HashMap<>();
    }

    public Map<String, YoutubeChannel> getYoutubeChannels() {
        return youtubeChannels;
    }

    public void addYoutubeChannel(YoutubeChannel channel) {
        youtubeChannels.put(channel.getChannelId(), channel);
    }

    public YoutubeChannel getYoutubeChannel(String channelId) {
        YoutubeChannel channel = youtubeChannels.get(channelId);
        return channel;
    }

    public void addYoutubeVideo(YoutubeVideo youtubeVideo) {
        youtubeVideos.put(youtubeVideo.getVideoId(), youtubeVideo);
    }

    public void linkEntities() {
        for (YoutubeVideo youtubeVideo : youtubeVideos.values()) {
            YoutubeChannel youtubeChannel = youtubeChannels.get(youtubeVideo.getChannelId());
            if (youtubeChannel != null) {
                youtubeChannel.addVideo(youtubeVideo);
            }
        }
    }
}
