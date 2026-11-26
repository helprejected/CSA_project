import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_YELLOW = "\u001b[33m";

    private static Computer computer;
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        Computer.Settings settings = Computer.Settings.load("config.txt");
        computer = new Computer(settings);

        printASCII();
        computer.togglePower();
        run();
    }

    //reads commands until the user exits
    public static void run() {
        boolean running = true;

        while (running) {
            System.out.print("\n> ");

            String line = input.nextLine();

            if (line.isEmpty()) {
                continue;
            }
            running = handle(line);
        }
    }

    //runs one command, returns false when the program should stop
    public static boolean handle(String line) {
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
        }

        else if (command.equals("status")) {
            computer.printComputerReport();
        }

        else if (command.equals("list")) {
            printFiles();
        }

        else if (command.equals("create")) {
            if (argument.isEmpty()) {
                System.out.println("usage: create <name>.txt");
            } else {
                computer.createFile(argument);
            }
        }

        else if (command.equals("write")) {
            if (argument.isEmpty()) {
                System.out.println(ANSI_YELLOW + "usage: write <name>.txt" + ANSI_RESET);
            } else if (computer.writeFile(argument)) {
                System.out.println("Edit and save the file in your editor, then press Enter here when you are done.");

                if (input.hasNextLine()) {
                    input.nextLine();
                }
                computer.saveModifiedDate(argument);
            }
        }

        else if (command.equals("read")) {
            if (argument.isEmpty()) {
                System.out.println(ANSI_YELLOW + "usage: read <file name>" + ANSI_RESET);
            } else {
                String content = computer.readFile(argument);

                if (content != null) {
                    System.out.println("====================================\n");
                    System.out.println(ANSI_YELLOW + content + ANSI_RESET);
                    System.out.println("====================================\n");
                }
            }
        }

        else if (command.equals("delete")) {
            if (argument.isEmpty()) {
                System.out.println(ANSI_YELLOW + "usage: delete <file name>" + ANSI_RESET);
            } else {
                computer.deleteFile(argument);
            }
        }

        else if (command.equals("inbox")) {
            printInbox();
        }

        else if (command.equals("check")) {
            if (argument.isEmpty()) {
                System.out.println(ANSI_YELLOW + "usage: check <file name>" + ANSI_RESET);
            } else {
                computer.checkImport(argument);
            }
        }

        else if (command.equals("import")) {
            if (argument.isEmpty()) {
                System.out.println(ANSI_YELLOW + "usage: import <file name>" + ANSI_RESET);
            } else {
                computer.importFile(argument);
            }
        }

        else if (command.equals("discard")) {
            if (argument.isEmpty()) {
                System.out.println(ANSI_YELLOW + "usage: discard <file name>" + ANSI_RESET);
            } else {
                computer.discardStaged(argument);
            }
        }

        else if (command.equals("exit")) {
            computer.togglePower();
            return false;
        }

        else {
            System.out.println("Unknown command. Type 'help' for a list.");
        }

        return true;
    }

    public static void printASCII() {
        try {
            Scanner reader = new Scanner(new File("Assets/ASCII_art.txt"));

            while (reader.hasNextLine()) {
                System.out.println(ANSI_CYAN + reader.nextLine() + ANSI_RESET);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("[placeholder starting banner here]");
        }
    }

    public static void printHelp() {
        System.out.println(ANSI_YELLOW);
        System.out.println("Commands:");
        System.out.println("  help          - show this list");
        System.out.println("  status        - full report of the computer");
        System.out.println("  list          - list the files in the storage");
        System.out.println("  create <file> - make a new empty .txt file, no need to add an extension");
        System.out.println("  write <file>  - open a file in your editor to edit it");
        System.out.println("  read <file>   - print text files, open other files");
        System.out.println("  delete <file> - remove a file from Storage");
        System.out.println("  inbox         - list the files waiting in staging");
        System.out.println("  check <file>  - test if a staged file fits the requirements");
        System.out.println("  import <file> - move a staged file into storage");
        System.out.println("  discard <file> - delete a staged file");
        System.out.println("  exit          - shut the computer down");
        System.out.println(ANSI_RESET);
    }

    public static void printFiles() {
        ArrayList<Computer.FileData> files = computer.listAllFiles();

        if (files.isEmpty()) {
            System.out.println(ANSI_YELLOW + "(Storage is empty)" + ANSI_RESET);
            return;
        }

        for (Computer.FileData file : files) {
            System.out.println("  " + file.getFullName());
        }
    }

    public static void printInbox() {
        ArrayList<Computer.FileData> files = computer.listStaging();

        if (files.isEmpty()) {
            System.out.println(ANSI_YELLOW + "(Validation is empty - drag a file into that folder first)" + ANSI_RESET);
            return;
        }

        for (Computer.FileData file : files) {
            String problem = computer.importError(file.getFullName());
            String status = (problem == null) ? "ready to import" : problem;

            System.out.println(ANSI_YELLOW + "  " + file.getFullName()
                    + "  (" + String.format("%.2f", file.getSizeKB()) + " KB)  -> " + status + ANSI_RESET);
        }
    }
}
