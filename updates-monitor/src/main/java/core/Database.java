package core;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import core.dto.YoutubeChannel;
import core.dto.YoutubeVideo;

public class Database {
    private Map<String, YoutubeChannel> youtubeChannels;
    private Map<String, YoutubeVideo> youtubeVideos;

    @JsonIgnore
    private Map<String, YoutubeVideo> youtubeNewVideos;
    @JsonIgnore
    private Map<String, YoutubeVideo> youtubeDownloadedVideos;

    public Database() {
        youtubeChannels = new HashMap<>();
        youtubeVideos = new HashMap<>();
        youtubeNewVideos = new HashMap<>();
        youtubeDownloadedVideos = new HashMap<>();
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
        youtubeNewVideos.clear();
        youtubeDownloadedVideos.clear();
        for (YoutubeVideo youtubeVideo : youtubeVideos.values()) {
            // Linking Youtube Videos with Youtube Channels
            YoutubeChannel youtubeChannel = youtubeChannels.get(youtubeVideo.getChannelId());
            if (youtubeChannel != null) {
                youtubeChannel.addVideo(youtubeVideo);
            }

            // Adding Youtube Videos to New and Downloaded Maps
            if (youtubeVideo.isDownloaded()) {
                youtubeDownloadedVideos.put(youtubeVideo.getVideoId(), youtubeVideo);
            } else {
                youtubeNewVideos.put(youtubeVideo.getVideoId(), youtubeVideo);
            }
        }
    }

    public Map<String, YoutubeVideo> getYoutubeVideos() {
        return youtubeVideos;
    }

    public Map<String, YoutubeVideo> getYoutubeNewVideos() {
        return youtubeNewVideos;
    }

    public Map<String, YoutubeVideo> getYoutubeDownloadedVideos() {
        return youtubeDownloadedVideos;
    }
}
