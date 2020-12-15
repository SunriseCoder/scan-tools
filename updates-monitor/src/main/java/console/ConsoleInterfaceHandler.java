package console;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;

import core.Database;
import core.dto.YoutubeChannel;
import core.youtube.YoutubeChannelHandler;
import core.youtube.YoutubeChannelHandler.UpdateResult;
import utils.JSONUtils;
import utils.ThreadUtils;

public class ConsoleInterfaceHandler {
    private Scanner scanner;
    private File databaseFile;
    private Database database;

    public ConsoleInterfaceHandler() {
        scanner = new Scanner(System.in);
    }

    public void start() throws IOException {
        System.out.println("Application is starting...");

        System.out.println("Loading database...");
        File databasePath = new File("database");
        if (!databasePath.exists()) {
            databasePath.mkdir();
        }
        databaseFile = new File(databasePath, "database.json");
        if (databaseFile.exists()) {
            try {
                TypeReference<Database> typeReference = new TypeReference<Database>() {};
                database = JSONUtils.loadFromDisk(databaseFile, typeReference);
            } catch (Exception e) {
                System.out.println("Could not read database from file " + databaseFile.getAbsolutePath() + ", creating a new one");
                e.printStackTrace();
                database = new Database();
            }
        } else {
            System.out.println("Database file " + databaseFile.getAbsolutePath() + " not found, creating a new Database");
            database = new Database();
        }

        System.out.println("Applicatoin started successfully");

        mainMenu();
    }

    private void mainMenu() throws IOException {
        String input;
        while (true) {
            System.out.print("Select action: [1] Status, [2] Add resource, [8] Check updates, [9] Download files, [0] Exit ");
            input = scanner.next();
            switch (input) {
            case "1":
                printStatus();
                break;
            case "2":
                addResource();
                break;
            case "8":
                checkUpdates();
                break;
            case "0":
                System.out.println("Exiting...");
                scanner.close();
                System.exit(0);
            default:
                System.out.println("Unknown command, please try again");
            }
        }
    }

    private void printStatus() {
        System.out.println("Youtube channels:");
        Collection<YoutubeChannel> youtubeChannels = database.getYoutubeChannels().values();
        for (YoutubeChannel channel : youtubeChannels) {
            System.out.println("\t" + channel.getStatusString());
        }
        System.out.println(youtubeChannels.size() + " channel(s) total");
    }

    private void addResource() {
        String input;
        boolean inputAcceptedFlag = false;
        while (!inputAcceptedFlag) {
            System.out.println("Please enter resource URL or cancel: ");
            input = scanner.next();
            input = input.trim();
            if ("cancel".equalsIgnoreCase(input)) {
                inputAcceptedFlag = true;
            } else if (YoutubeChannelHandler.isURLAChannel(input)) {
                try {
                    String channelId = YoutubeChannelHandler.parseChannelId(input);
                    if (database.getYoutubeChannel(channelId) == null) {
                        // Adding Youtube Channel to the Database
                        YoutubeChannel channel = new YoutubeChannel(channelId);
                        database.addYoutubeChannel(channel);
                        saveDatabase();
                        System.out.println("Youtube Channel with ID: " + channelId + " has beed added successfully");
                    } else {
                        System.out.println("Youtube Channel with ID: " + channelId + " is already in the database");
                    }
                } catch (Exception e) {
                    System.out.println("Could not add (" + input + ") as Youtube channel becase of: " + e.getMessage());
                }
            } else {
                inputAcceptedFlag = false;
                System.out.println("Unsupported resource URL: (" + input + ")");
            }
        }
    }

    private void saveDatabase() throws IOException {
        JSONUtils.saveToDisk(database, databaseFile);
    }

    private void checkUpdates() throws IOException {
        System.out.println("Updating Youtube Channels...");
        Iterator<Entry<String, YoutubeChannel>> youtubeChannelsIterator = database.getYoutubeChannels().entrySet().iterator();
        while (youtubeChannelsIterator.hasNext()) {
            Entry<String, YoutubeChannel> channelEntry = youtubeChannelsIterator.next();
            YoutubeChannel channel = channelEntry.getValue();
            boolean updateSuccess = false;
            int attempts = 10;
            do {
                try {
                    System.out.print("\tUpdating Youtube Channel " + channel + "... ");
                    UpdateResult updateResult = YoutubeChannelHandler.checkUpdates(channel);
                    if (updateResult.channelNotFound) {
                        System.out.println("Channel not found on Youtube!!!");
                    } else {
                        System.out.println("Successfully");
                        if (!updateResult.newTitle.equals(updateResult.oldTitle)) {
                            System.out.println("\t\tYoutube Channel " + channel.getChannelId() + " Title was changed from \""
                                            + updateResult.oldTitle + "\" to \"" + updateResult.newTitle + "\"");
                        }
                        // TODO Add update details about videos and playlists
                        saveDatabase();
                    }
                    updateSuccess = true;
                } catch (Exception e) {
                    System.out.println("Error due to update channel: " + channel + ": " + e.getMessage());
                    e.printStackTrace();
                    ThreadUtils.sleep(5000);
                    attempts--;
                    if (attempts == 0) {
                        System.out.println("No attempts to retry left, skipping channel for now...");
                    }
                }
            } while (!updateSuccess);
        }
    }
}
