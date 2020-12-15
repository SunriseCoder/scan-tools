package core;

import java.util.HashMap;
import java.util.Map;

import core.dto.YoutubeChannel;

public class Database {
    Map<String, YoutubeChannel> youtubeChannels;

    public Database() {
        youtubeChannels = new HashMap<>();
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
}
