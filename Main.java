import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The terminal of the simulated computer.
 *
 * This class only talks to the user: it reads a line, works out which command
 * it is, and calls the matching method on the Computer. All of the rules
 * (storage limits, battery, file types) live in Computer.java.
 */
public class Main {

    private static Computer computer;
    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        Computer.Settings settings = Computer.Settings.load("config.json");
        computer = new Computer(settings);

        printBanner();
        computer.togglePower();
        run();
    }

    /** Reads commands until the user exits or the battery runs out. */
    private static void run() {
        boolean running = true;

        while (running) {
            if (computer.checkBatteryStatus() <= 0) {
                System.out.println("Battery empty - shutting down...");
                computer.togglePower();
                break;
            }

            System.out.print("> ");
            String line = INPUT.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }
            running = handle(line);
        }
    }

    /** Runs one command. Returns false when the program should stop. */
    private static boolean handle(String line) {
        String command = line;
        String argument = "";

        int space = line.indexOf(' ');
        if (space != -1) {
            command = line.substring(0, space);
            argument = line.substring(space + 1).trim();
        }
        command = command.toLowerCase();

        if (command.equals("help")) {
            printHelp();
        } else if (command.equals("status")) {
            computer.printComputerReport();
        } else if (command.equals("ls")) {
            printFiles();
        } else if (command.equals("read")) {
            if (argument.isEmpty()) {
                System.out.println("Usage: read <file name>");
            } else {
                String content = computer.readFile(argument);
                if (content != null) {
                    System.out.println(content);
                }
            }
        } else if (command.equals("exit")) {
            computer.togglePower();
            return false;
        } else {
            System.out.println("Unknown command. Type 'help' for a list.");
        }

        return true;
    }

    private static void printBanner() {
        File art = new File("Assets/ASCII_art.txt");

        try (Scanner reader = new Scanner(art)) {
            while (reader.hasNextLine()) {
                System.out.println(reader.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("MiniOS - a tiny simulated computer");
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help          - show this list");
        System.out.println("  status        - full report of the computer");
        System.out.println("  ls            - list the files in Storage");
        System.out.println("  read <file>   - print the content of a file");
        System.out.println("  exit          - shut the computer down");
    }

    private static void printFiles() {
        ArrayList<Computer.FileData> files = computer.listAllFiles();

        if (files.isEmpty()) {
            System.out.println("(Storage is empty)");
            return;
        }

        for (Computer.FileData file : files) {
            System.out.println("  " + file.fileProperties());
        }
    }
}
