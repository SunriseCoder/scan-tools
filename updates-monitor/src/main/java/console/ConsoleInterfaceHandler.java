package console;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;

import core.Database;
import core.ResourceTypeHandler;
import core.dto.YoutubeChannel;
import utils.JSONUtils;

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

    private void mainMenu() {
        String input;
        while (true) {
            System.out.print("Select action: [1] Status, [2] Add resource, [0] Exit ");
            input = scanner.next();
            switch (input) {
            case "1":
                printStatus();
                break;
            case "2":
                addResource();
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
            System.out.println(channel.getStatusString());
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
            } else if (ResourceTypeHandler.isURLAYoutubeChannel(input)) {
                try {
                    String channelId = ResourceTypeHandler.parseYoutubeChannelId(input);
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
}
