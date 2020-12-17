package core.youtube;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;

import core.dto.YoutubeChannel;
import core.dto.YoutubeVideo;
import util.DownloadUtils;
import util.DownloadUtils.Response;
import utils.JSONUtils;

public class YoutubeChannelHandler {
    private static final Pattern CHANNEL_URL_PATTERN = Pattern.compile("^https?://www.youtube.com/channel/([0-9A-Za-z_-]+)/?.*$");
    private static final Pattern CHANNEL_CUSTOM_URL_PATTERN = Pattern.compile("^https?://www.youtube.com/c/([^/\\s]+)/?.*$");
    private static final Pattern CHANNEL_USER_URL_PATTERN = Pattern.compile("^https?://www.youtube.com/user/([0-9A-Za-z_-]+)/?.*$");

    public static boolean isYoutubeChannelURL(String url) {
        Matcher matcher = CHANNEL_URL_PATTERN.matcher(url);
        boolean result = matcher.matches() && matcher.groupCount() == 1;

        matcher = CHANNEL_CUSTOM_URL_PATTERN.matcher(url);
        result |= matcher.matches() && matcher.groupCount() == 1;

        matcher = CHANNEL_USER_URL_PATTERN.matcher(url);
        result |= matcher.matches() && matcher.groupCount() == 1;

        return result;
    }

    public static Result fetchNewChannel(String url) throws IOException {
        Result result = new Result();

        YoutubeChannel channel = new YoutubeChannel();
        result.youtubeChannel = channel;

        Result channelIdResult = downloadChannelIdByCustomURL(url);
        if (channelIdResult.channelNotFound) {
            result.channelNotFound = channelIdResult.channelNotFound;
            return result;
        }
        channel.setChannelId(channelIdResult.channelId);

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

    public static Result checkUpdates(YoutubeChannel channel) throws IOException {
        Result result = new Result();
        result.oldTitle = channel.getTitle();

        // Checking Channel Title
        Result downloadTitleResult = downloadChannelTitle(channel.getChannelId());
        String channelTitle = downloadTitleResult.newTitle;
        result.newTitle = channelTitle;

        // Fetching Videos from Channel
        Result updateChannelVideosResult = updateChannelVideos(channel);
        result.newVideos = updateChannelVideosResult.newVideos;
        result.channelNotFound = result.channelNotFound;

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
        Connection connection = Jsoup.connect(url);
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

    private static Result updateChannelVideos(YoutubeChannel channel) throws IOException {
        Result result = new Result();
        result.newVideos = new ArrayList<>();

        // Parsing the first page with videos
        System.out.print("1 "); // Number of page we are parsing
        Result videoListResult = downloadVideosListFirstPage(channel.getChannelId());
        if (videoListResult.channelNotFound) {
            result.channelNotFound = videoListResult.channelNotFound;
            return result;
        }

        for (YoutubeVideo video : videoListResult.newVideos) {
            if (channel.containVideo(video.getVideoId())) {
                return result;
            }

            channel.addVideo(video);
            result.newVideos.add(video);
        }

        // Parsing the other pages
        int pageNumber = 2;
        while (videoListResult.continuationToken != null) {
            System.out.print(pageNumber++ + " ");
            videoListResult = downloadVideosListNextPage(channel.getChannelId(), videoListResult);
            if (videoListResult.channelNotFound) {
                result.channelNotFound = videoListResult.channelNotFound;
                return result;
            }

            for (YoutubeVideo video : videoListResult.newVideos) {
                if (channel.containVideo(video.getVideoId())) {
                    return result;
                }

                channel.addVideo(video);
                result.newVideos.add(video);
            }
        }

        return result;
    }

    private static Result downloadVideosListFirstPage(String channelId) throws IOException {
        List<YoutubeVideo> videos = new ArrayList<>();
        Result result = new Result();
        result.newVideos = videos;

        String urlString = "https://www.youtube.com/channel/" + channelId + "/videos";
        Response response = DownloadUtils.downloadPage(urlString, null);

        if (response.headers.get(null).get(0).split(" ")[1].equals("404")) {
            result.channelNotFound = true;
            return result;
        }

        try {
            Document videosPage = Jsoup.parse(response.body);
            String pageData = null;
            Elements scriptNodes = videosPage.select("script");
            for (Element scriptNode : scriptNodes) {
                if (scriptNode.data().contains("var ytInitialData = ")) {
                    pageData = scriptNode.data();
                    break;
                }
            }
            String jsonText = pageData.replace("var ytInitialData = ", "");
            jsonText = jsonText.substring(0, jsonText.length() - 1); // Cutting semicolon at the end off
            JsonNode jsonRootNode = JSONUtils.parseJSON(jsonText);

            JsonNode jsonNode = jsonRootNode.get("responseContext");
            JsonNode stpNode = jsonNode.get("serviceTrackingParams");
            for (JsonNode serviceNode : stpNode) {
                if ("CSI".equals(serviceNode.get("service").asText())) {
                    JsonNode paramsNode = serviceNode.get("params");
                    for (JsonNode paramNode : paramsNode) {
                        if ("cver".equals(paramNode.get("key").asText())) {
                            result.clientVersion = paramNode.get("value").asText();
                        }
                    }
                }
            }

            jsonNode = jsonRootNode.get("contents");
            jsonNode = jsonNode.get("twoColumnBrowseResultsRenderer");
            jsonNode = jsonNode.get("tabs");
            jsonNode = jsonNode.get(1);
            jsonNode = jsonNode.get("tabRenderer");
            jsonNode = jsonNode.get("content");
            jsonNode = jsonNode.get("sectionListRenderer");
            jsonNode = jsonNode.get("contents");
            jsonNode = jsonNode.get(0);
            jsonNode = jsonNode.get("itemSectionRenderer");
            jsonNode = jsonNode.get("contents");
            jsonNode = jsonNode.get(0);

            if (jsonNode.has("gridRenderer")) {
                JsonNode gridRendererNode = jsonNode.get("gridRenderer");
                JsonNode videoNodes = gridRendererNode.get("items");

                for (JsonNode videoNode : videoNodes) {
                    YoutubeVideo video = new YoutubeVideo();
                    video.setChannelId(channelId);

                    String videoId = videoNode.get("gridVideoRenderer").get("videoId").asText();
                    video.setVideoId(videoId);

                    String videoTitle = videoNode.get("gridVideoRenderer").get("title").get("runs").get(0).get("text").asText();
                    video.setTitle(videoTitle);

                    videos.add(video);
                }

                if (gridRendererNode.has("continuations")) {
                    JsonNode continuationsNode = gridRendererNode.get("continuations");
                    continuationsNode = continuationsNode.get(0);
                    continuationsNode = continuationsNode.get("nextContinuationData");
                    result.continuationToken = continuationsNode.get("continuation").asText();
                    result.clickTrackingParams = continuationsNode.get("clickTrackingParams").asText();
                }
            }
        } catch (Exception e) {
            PrintWriter pw = new PrintWriter("error-page-dump.dat");
            pw.print(response.body);
            pw.flush();
            pw.close();
            throw new RuntimeException("Could not parse Youtube Channel Videos page, Youtube page design was probably changed: " + e.getMessage(), e);
        }

        return result;
    }

    private static Result downloadVideosListNextPage(String channelId, Result lastResult) throws IOException {
        List<YoutubeVideo> videos = new ArrayList<>();
        Result result = new Result();
        result.newVideos = videos;

        try {
            String url = "https://www.youtube.com/browse_ajax?ctoken=" + URLEncoder.encode(lastResult.continuationToken, "UTF-8") + "&continuation="
                    + URLEncoder.encode(lastResult.continuationToken, "UTF-8") + "&itct=" + URLEncoder.encode(lastResult.clickTrackingParams, "UTF-8");
            Map<String, String> headers = generateHeaders(channelId, lastResult);
            Response response = DownloadUtils.downloadPage(url, headers);
            String jsonText = response.body;
            JsonNode jsonNode = JSONUtils.parseJSON(jsonText);
            if (!jsonNode.isArray()) {
                // Request always returns Status 200 OK, but different content
                result.channelNotFound = true;
                return result;
            }

            jsonNode = jsonNode.get(1);
            jsonNode = jsonNode.get("response");
            if (jsonNode.has("continuationContents")) {
                jsonNode = jsonNode.get("continuationContents");
                if (jsonNode.has("gridContinuation")) {
                    JsonNode gridContinuationNode = jsonNode.get("gridContinuation");
                    if (gridContinuationNode.has("items")) {
                        // Parsing Videos
                        JsonNode videoNodes = gridContinuationNode.get("items");
                        for (JsonNode videoNode : videoNodes) {
                            YoutubeVideo video = new YoutubeVideo();
                            video.setChannelId(channelId);

                            String videoId = videoNode.get("gridVideoRenderer").get("videoId").asText();
                            video.setVideoId(videoId);

                            String videoTitle = videoNode.get("gridVideoRenderer").get("title").get("runs").get(0).get("text").asText();
                            video.setTitle(videoTitle);

                            videos.add(video);
                        }
                    }

                    // Parsing Continuation parameters
                    if (gridContinuationNode.has("continuations")) {
                        JsonNode continuationsNode = gridContinuationNode.get("continuations");
                        continuationsNode = continuationsNode.get(0);
                        continuationsNode = continuationsNode.get("nextContinuationData");
                        result.continuationToken = continuationsNode.get("continuation").asText();
                        result.clickTrackingParams = continuationsNode.get("clickTrackingParams").asText();
                        result.clientVersion = lastResult.clientVersion;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not parse Youtube Channel Videos page, Youtube page design was probably changed: " + e.getMessage(), e);
        }

        return result;
    }

    private static Map<String, String> generateHeaders(String channelId, Result lastResult) {
        Map<String, String> headers = new HashMap<>();

        headers.put("x-youtube-client-name", "1");
        headers.put("x-youtube-client-version", lastResult.clientVersion);

        return headers;
    }

    public static class Result {
        public YoutubeChannel youtubeChannel;
        public boolean channelNotFound = false;
        public String oldTitle;
        public String newTitle;
        public String channelId;
        public List<YoutubeVideo> newVideos;
        public String clientVersion;
        public String continuationToken;
        public String clickTrackingParams;
    }
}
