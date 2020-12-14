package console;

import java.util.Scanner;

public class ConsoleInterfaceHandler {

    public void start() {
        System.out.println("Application is starting...");
        System.out.println("Applicatoin started successfully");

        mainMenu();
    }

    private void mainMenu() {
        System.out.print("Select action: [0] Exit ");

        Scanner scanner = new Scanner(System.in);
        String input;
        boolean inputAcceptedFlag = false;
        while (!inputAcceptedFlag) {
            input = scanner.next();
            inputAcceptedFlag = true;
            switch (input) {
            case "0":
                System.out.println("Exiting...");
                System.exit(0);
            default:
                inputAcceptedFlag = false;
            }
        }
        scanner.close();
    }
}
