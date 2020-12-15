package core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceTypeHandler {
    private static final Pattern YOUTUBE_CHANNEL_PATTERN = Pattern.compile("^https?://www.youtube.com/channel/([0-9A-Za-z]+)$");

    public static boolean isURLAYoutubeChannel(String url) {
        Matcher matcher = YOUTUBE_CHANNEL_PATTERN.matcher(url);
        boolean result = matcher.matches();
        result &= matcher.groupCount() == 1;
        return result;
    }

    public static String parseYoutubeChannelId(String url) {
        // Fetching Youtube Channel Id
        Matcher matcher = YOUTUBE_CHANNEL_PATTERN.matcher(url);
        if (matcher.matches()) {
            String channelId = matcher.group(1);
            return channelId;
        } else {
            throw new RuntimeException("URL: (" + url + ") is not a Youtube Channel");
        }
    }
}
