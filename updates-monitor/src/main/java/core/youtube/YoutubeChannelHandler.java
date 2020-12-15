package core.youtube;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import core.dto.YoutubeChannel;

public class YoutubeChannelHandler {
    private static final Pattern CHANNEL_URL_PATTERN = Pattern
            .compile("^https?://www.youtube.com/channel/([0-9A-Za-z]+)$");

    public static boolean isURLAChannel(String url) {
        Matcher matcher = CHANNEL_URL_PATTERN.matcher(url);
        boolean result = matcher.matches();
        result &= matcher.groupCount() == 1;
        return result;
    }

    public static String parseChannelId(String url) {
        // Fetching Youtube Channel Id
        Matcher matcher = CHANNEL_URL_PATTERN.matcher(url);
        if (matcher.matches() && matcher.groupCount() > 0) {
            String channelId = matcher.group(1);
            return channelId;
        } else {
            throw new RuntimeException("URL: (" + url + ") is not a Youtube Channel");
        }
    }

    public static UpdateResult checkUpdates(YoutubeChannel channel) throws IOException {
        UpdateResult updateResult = new UpdateResult();
        updateResult.oldTitle = channel.getTitle();

        Connection connection = Jsoup.connect("https://www.youtube.com/channel/" + channel.getChannelId());
        if (connection.response().statusCode() == 404) {
            updateResult.channelNotFound = true;
            return updateResult;
        }

        Document channelPage = connection.get();
        String channelTitle = channelPage.select("meta[itemprop=name]").attr("content");
        if (channelTitle == null || channelTitle.isEmpty()) {
            throw new RuntimeException("Could not parse Youtube Channel Title, page format probably was changed,"
                    + " please try newer version or contact developers to update parsing algorithm");
        }

        channel.setTitle(channelTitle);
        updateResult.newTitle = channelTitle;

        // TODO Implement Videos fetch

        // TODO Implement Playlists fetch
        // TODO All videos from playlists try to check, if they are already on the channel,
        //      if not (probably unlisted) - add them to channel for further download
        // TODO Need to think about videos from another channels, but in the playlists of current channels
        //      maybe add another channels, but with flag to not to download them

        return updateResult;
    }

    public static class UpdateResult {
        public boolean channelNotFound = false;
        public String oldTitle;
        public String newTitle;
    }
}
