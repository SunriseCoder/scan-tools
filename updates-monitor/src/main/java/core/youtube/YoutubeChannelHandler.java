package core.youtube;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import core.dto.YoutubeChannel;

public class YoutubeChannelHandler {
    private static final Pattern CHANNEL_URL_PATTERN = Pattern.compile("^https?://www.youtube.com/channel/([0-9A-Za-z_-]+)$");
    private static final Pattern CHANNEL_CUSTOM_URL_PATTERN = Pattern.compile("^https?://www.youtube.com/c/([^/\\s]+)/?.*$");

    public static boolean isChannelURL(String url) {
        Matcher matcher = CHANNEL_URL_PATTERN.matcher(url);
        boolean result = matcher.matches() && matcher.groupCount() == 1;
        return result;
    }

    public static boolean isChannelCustomURL(String url) {
        Matcher matcher = CHANNEL_CUSTOM_URL_PATTERN.matcher(url);
        boolean result = matcher.matches() && matcher.groupCount() == 1;
        return result;
    }

    public static Result fetchNewChannel(String url) throws IOException {
        Result result = new Result();

        YoutubeChannel channel = new YoutubeChannel();
        result.youtubeChannel = channel;

        if (isChannelURL(url)) {
            String channelId = parseChannelId(url);
            channel.setChannelId(channelId);
        } else if (isChannelCustomURL(url)) {
            String customURL = parseChannelCustomURL(url);
            Result channelIdResult = downloadChannelIdByCustomURL(customURL);
            if (channelIdResult.channelNotFound) {
                result.channelNotFound = channelIdResult.channelNotFound;
                return result;
            }

            channel.setChannelId(channelIdResult.channelId);
        }

        Result downloadTitleResult = downloadChannelTitle(channel.getChannelId());
        if (downloadTitleResult.channelNotFound) {
            result.channelNotFound = downloadTitleResult.channelNotFound;
            return result;
        }
        channel.setTitle(downloadTitleResult.newTitle);

        return result;
    }

    public static String parseChannelId(String url) {
        Matcher matcher = CHANNEL_URL_PATTERN.matcher(url);
        if (matcher.matches() && matcher.groupCount() > 0) {
            String channelId = matcher.group(1);
            return channelId;
        } else {
            throw new RuntimeException("URL: (" + url + ") is not a Youtube Channel");
        }
    }

    public static String parseChannelCustomURL(String url) {
        Matcher matcher = CHANNEL_CUSTOM_URL_PATTERN.matcher(url);
        if (matcher.matches() && matcher.groupCount() > 0) {
            String channelId = matcher.group(1);
            return channelId;
        } else {
            throw new RuntimeException("URL: (" + url + ") is not a Youtube Channel");
        }
    }

    public static Result checkUpdates(YoutubeChannel channel) throws IOException {
        Result result = new Result();
        result.oldTitle = channel.getTitle();

        // Checking Channel Title
        Result downloadTitleResult = downloadChannelTitle(channel.getChannelId());
        String channelTitle = downloadTitleResult.newTitle;
        channel.setTitle(channelTitle);
        result.newTitle = channelTitle;

        // TODO Implement Videos fetch

        // TODO Implement Playlists fetch
        // TODO All videos from playlists try to check, if they are already on the channel,
        //      if not (probably unlisted) - add them to channel for further download
        // TODO Need to think about videos from another channels, but in the playlists of current channels
        //      maybe add another channels, but with flag to not to download them

        return result;
    }

    private static Result downloadChannelTitle(String channelId) throws IOException {
        Result result = new Result();
        Connection connection = Jsoup.connect("https://www.youtube.com/channel/" + channelId);
        if (connection.response().statusCode() == 404) {
            result.channelNotFound = true;
            return result;
        }

        Document channelPage = connection.get();
        String channelTitle = channelPage.select("meta[itemprop=name]").attr("content");
        if (channelTitle == null || channelTitle.isEmpty()) {
            throw new RuntimeException("Could not parse Youtube Channel Title, page format probably was changed,"
                    + " please try newer version or contact developers to update parsing algorithm");
        }
        result.newTitle = channelTitle;
        return result;
    }

    private static Result downloadChannelIdByCustomURL(String url) throws IOException {
        Result result = new Result();
        Connection connection = Jsoup.connect("https://www.youtube.com/c/" + url);
        if (connection.response().statusCode() == 404) {
            result.channelNotFound = true;
            return result;
        }

        Document channelPage = connection.get();
        String channelId = channelPage.select("meta[itemprop=channelId]").attr("content");
        if (channelId == null || channelId.isEmpty()) {
            throw new RuntimeException("Could not parse Youtube Channel ID, page format probably was changed,"
                    + " please try newer version or contact developers to update parsing algorithm");
        }

        result.channelId = channelId;
        return result;
    }

    public static class Result {
        public YoutubeChannel youtubeChannel;
        public boolean channelNotFound = false;
        public String oldTitle;
        public String newTitle;
        public String channelId;
    }
}
