import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Computer {

    private static final String STORAGE_DIR = "Storage";
    private static final String STAGING_DIR = "Validation";

    //dictionary to store the added and modified dates of the files since no way to access the creation date through system
    private static final String DICT_FILE = "filetimes.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    //supported text type that is not displayed in binary like for example mp3 files, since forcefully opening up mp3 files will just give you raw data
    private static final String[] TEXT_TYPES = { "txt", "md", "csv", "json" };

    //Settings from the config files below here

    public static class Settings {

        private String osName;
        private String computerName;
        private double maxStorageKB;
        private ArrayList<String> writableFileTypes;
        private ArrayList<String> readableFileTypes;

        //default settings

        public Settings() {
            osName = "test-os";
            computerName = "my-computer";
            maxStorageKB = 25600.0;
            writableFileTypes = splitTypes("txt");
            readableFileTypes = splitTypes("txt,md,json,csv,mp3,wav,mp4,jpg,png,gif,svg,html,css,js");
        }

        //load configs from config file --> constructor
        public static Settings load(String configPath) {
            Settings settings = new Settings();
            File configFile = new File(configPath);

            if (!configFile.exists()) {
                return settings;
            }

            try {
                Scanner reader = new Scanner(configFile);

                while (reader.hasNextLine()) {
                    String line = reader.nextLine().trim();

                    if (line.isEmpty()) {
                        continue;
                    }

                    int equals = line.indexOf('=');

                    if (equals == -1) {
                        continue;
                    }

                    String key = line.substring(0, equals).trim();
                    String value = line.substring(equals + 1).trim();
                    settings.apply(key, value);
                }

                reader.close();
            } catch (Exception e) {
                return new Settings();
            }

            return settings;
        }

        public void apply(String key, String value) {
            try {
                if (key.equals("osName")) {
                    osName = value;
                }

                else if (key.equals("computerName")) {
                    computerName = value;
                }

                else if (key.equals("maxStorageKB")) {
                    maxStorageKB = Double.parseDouble(value);
                }

                else if (key.equals("writableFileTypes")) {
                    writableFileTypes = splitTypes(value);
                }

                else if (key.equals("readableFileTypes")) {
                    readableFileTypes = splitTypes(value);
                }

            } catch (NumberFormatException e) {
                System.out.println("max storage kb must be a number");
            }
        }

        // eg txt md
        public static ArrayList<String> splitTypes(String csv) {
            ArrayList<String> types = new ArrayList<String>();

            for (String type : csv.split(",")) {
                type = type.trim().toLowerCase();
                if (!type.isEmpty()) {
                    types.add(type);
                }
            }
            return types;
        }

        //basic getters for settings
        public String getOsName() {return osName;}
        public String getComputerName() {return computerName;}
        public double getMaxStorageKB() {return maxStorageKB;}
        public ArrayList<String> getWritableFileTypes() {return writableFileTypes;}
        public ArrayList<String> getReadableFileTypes() {return readableFileTypes;}

    }

    //filedata here

    public static class FileData {

        private String fileName;
        private String fileType;
        private String addedDate;
        private String modifiedDate;
        private double sizeKB;

        public FileData(String name, String type, String added, String modified, double sizeKB) {
            fileName = name;
            fileType = type;
            addedDate = added;
            modifiedDate = modified;
            this.sizeKB = sizeKB;
        }

        //builds one filedata from a real file, the dates come from the dictionary
        public static FileData fromFile(File file, String added, String modified) {
            String fullName = file.getName();
            int dot = fullName.lastIndexOf('.');
            String name = (dot == -1) ? fullName : fullName.substring(0, dot);
            String type = (dot == -1) ? "" : fullName.substring(dot + 1).toLowerCase();

            return new FileData(name, type, added, modified, file.length() / 1024.0);
        }

        //basic getters for filedata
        public String getFileType() {return fileType;}
        public String getAddedDate() {return addedDate;}
        public double getSizeKB() {return sizeKB;}
        public String getFullName() {return fileName + "." + fileType;}

        public String fileProperties() {
            String added = (addedDate == null) ? "unknown" : addedDate;
            String modified = (modifiedDate == null) ? "unknown" : modifiedDate;

            return "File: " + getFullName() + " / Type: " + fileType
                    + " / Added: " + added + " / Modified: " + modified
                    + " / Size: " + formatSize(sizeKB);
        }

    }

    //the main pc machine below here

    private Settings settings;
    private File storageFolder;
    private File stagingFolder;
    private File dictFile;
    private boolean isPoweredOn;

    public Computer(Settings settings) {
        this.settings = settings;
        this.storageFolder = new File(STORAGE_DIR);
        this.stagingFolder = new File(STAGING_DIR);
        this.dictFile = new File(DICT_FILE);

        storageFolder.mkdirs();
        stagingFolder.mkdirs();

        isPoweredOn = false;
    }

    //power

    public void togglePower() {
        if (isPoweredOn) {
            isPoweredOn = false;
            System.out.println("Shutting down...");
        } else {
            isPoweredOn = true;
            System.out.println("Booting up " + settings.getComputerName() + "...");
        }
    }

    //storage space

    public double getStorageSpace() {return settings.getMaxStorageKB();}

    //adds up the size of every real file in Storage
    public double calcStorageSpace() {
        double usedKB = 0.0;

        for (File file : storageFolder.listFiles()) {
            if (file.isFile()) {
                usedKB += file.length() / 1024.0;
            }
        }
        return usedKB;
    }

    //check how much room left so you cant put more files in if full
    public double freeStorageSpace() {
        double val = (getStorageSpace() - calcStorageSpace()>0.0)? getStorageSpace() - calcStorageSpace() : 0.0;
        return val;
    }
    
    public static String formatSize(double sizeKB) {
        if (sizeKB >= 1024.0) {
            return String.format("%.2f MB", sizeKB / 1024.0);
        }
        return String.format("%.2f KB", sizeKB);

    }
    public String getStorageSpaceData() {
        return formatSize(calcStorageSpace()) + " used / " + formatSize(getStorageSpace())
                + " total (" + formatSize(freeStorageSpace()) + " free)";
    }

    //file operations

    //returns the added date of a file, null when it has no record
    public String findAddedDate(String fullName) {
        String found = null;

        try {
            Scanner reader = new Scanner(dictFile);

            while (reader.hasNextLine()) {
                String[] parts = reader.nextLine().split("\\|");

                if (parts.length == 3 && parts[0].trim().equals(fullName)) {
                    found = parts[1].trim();
                }
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("no dictionary yet");
        }
        return found;
    }

    //same as method above but for the modified date
    public String findModifiedDate(String fullName) {
        String found = null;

        try {
            Scanner reader = new Scanner(dictFile);

            while (reader.hasNextLine()) {
                String[] parts = reader.nextLine().split("\\|");

                if (parts.length == 3 && parts[0].trim().equals(fullName)) {
                    found = parts[2].trim();
                }
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("no dictionary yet");
        }
        return found;
    }

    public ArrayList<FileData> listAllFiles() {
        ArrayList<FileData> files = new ArrayList<FileData>();

        for (File file : storageFolder.listFiles()) {
            if (file.isFile()) {
                String added = findAddedDate(file.getName());
                String modified = findModifiedDate(file.getName());
                files.add(FileData.fromFile(file, added, modified));
            }
        }
        return files;
    }

    public FileData findFile(String fileName) {
        ArrayList<FileData> files = listAllFiles();

        for (FileData data : files) {
            if (data.getFullName().equals(fileName)) {
                return data;
            }
        }
        return null;
    }

    //returns the text inside a file
    public String readFile(String fileName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return null;
        }

        FileData data = findFile(fileName);

        if (data == null) {
            System.out.println("No such file: " + fileName);
            return null;
        }

        if (!isReadableType(data.getFileType())) {
            System.out.println("This computer cannot open ." + data.getFileType() + " files.");
            return null;
        }

        //not a text file so open it with the program windows uses for that type
        if (!isTextType(data.getFileType())) {
            System.out.println("Opening " + data.getFullName() + "...");
            openExternally(new File(storageFolder, data.getFullName()));
            return null;
        }

        String text = "";

        try {
            Scanner reader = new Scanner(new File(storageFolder, data.getFullName()));

            while (reader.hasNextLine()) {
                if (text.length() > 0) {
                    text = text + "\n";
                }
                text = text + reader.nextLine();
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("Could not read " + fileName + ".");
            return null;
        }

        return text;
    }

    //creating writing and deleting

    //creates an empty file in storage, returns false when it is not allowed due to disk space limitarions
        //notes.txt -> txt, returns "" when there is no extension
    public static String extensionOf(String fullName) {
        int dot = fullName.lastIndexOf('.');

        if (dot == -1 || dot == fullName.length() - 1) {
            return "";
        }
        return fullName.substring(dot + 1).toLowerCase();
    }

    public boolean createFile(String fullName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return false;
        }

        if (!isValidName(fullName)) {
            System.out.println("File names need an extension, like '.txt' for example");
            return false;
        }

        if (!isWritableType(extensionOf(fullName))) {
            System.out.println("Cannot create ." + extensionOf(fullName) + " files. Allowed types: "
                    + settings.getWritableFileTypes());
            return false;
        }

        if (findFile(fullName) != null) {
            System.out.println("A file called " + fullName + " already exists.");
            return false;
        }

        if (calcStorageSpace() >= getStorageSpace()) {
            System.out.println("Storage is full.");
            return false;
        }

        try {
            new File(storageFolder, fullName).createNewFile();
        } catch (Exception e) {
            System.out.println("Could not create " + fullName + ".");
            return false;
        }

        saveFileTimes(fullName, getCurrentTime(), getCurrentTime());
        System.out.println("Created " + fullName + ".");
        return true;
    }

    //opens a text file in the default editor so it can be edited by hand
    public boolean writeFile(String fullName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return false;
        }

        FileData data = findFile(fullName);

        if (data == null) {
            System.out.println("No such file: " + fullName + ". Create it first.");
            return false;
        }

        if (!isWritableType(data.getFileType())) {
            System.out.println("Cannot write to ." + data.getFileType() + " files.");
            return false;
        }

        System.out.println("Opening " + data.getFullName() + " in your editor...");
        return openForEditing(new File(storageFolder, data.getFullName()));
    }

    //saves the new modified date after the file was edited and prints the new size
    public void saveModifiedDate(String fullName) {
        FileData data = findFile(fullName);

        if (data == null) {
            return;
        }

        File realFile = new File(storageFolder, data.getFullName());
        String added = (data.getAddedDate() == null) ? "unknown" : data.getAddedDate();

        saveFileTimes(fullName, added, getCurrentTime());
        System.out.println("Saved " + data.getFullName() + " (" + formatSize(realFile.length() / 1024.0) + ").");
    }

    public boolean deleteFile(String fullName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return false;
        }

        FileData data = findFile(fullName);

        if (data == null) {
            System.out.println("No such file: " + fullName);
            return false;
        }

        new File(storageFolder, data.getFullName()).delete();

        //the dictionary keeps the old record, a later record for the same name wins
        System.out.println("Deleted " + data.getFullName() + ".");
        return true;
    }

    //validation file inbox

    //reads Validation and returns one filedata per file, they have no dates yet
    public ArrayList<FileData> listStaging() {
        ArrayList<FileData> files = new ArrayList<FileData>();

        for (File file : stagingFolder.listFiles()) {
            if (file.isFile()) {
                files.add(FileData.fromFile(file, null, null));
            }
        }
        return files;
    }

    //the import rules needed in the validation stage and null means that there is no import error identified
    public String importError(String stagedName) {
        if (!isValidName(stagedName)) {
            return "the name needs an extension";
        }

        File staged = new File(stagingFolder, stagedName);

        if (!staged.isFile()) {
            return "no file called " + stagedName + " in Validation folder";
        }

        //the type has to be one this computer knows how to open
        String type = extensionOf(stagedName);

        if (!isReadableType(type)) {
            return "the ." + type + " file type is not supported";
        }

        if (findFile(stagedName) != null) {
            return stagedName + " already exists in Storage";
        }

        double sizeKB = staged.length() / 1024.0;

        if (calcStorageSpace() + sizeKB > getStorageSpace()) {
            return "There is not enough disk space left to be saved in the system";
        }

        return null;
    }

    //the actual checking process before importing the file into storage
    public boolean checkImport(String stagedName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return false;
        }

        String problem = importError(stagedName);

        if (problem == null) {
            System.out.println("OK : " + stagedName + " can be imported.");
            return true;
        }

        System.out.println("Cannot import " + stagedName + ": " + problem + ".");
        return false;
    }

    //moves a staged file into Storage and saves its dates
    public boolean importFile(String stagedName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return false;
        }

        String problem = importError(stagedName);

        if (problem != null) {
            System.out.println("Cannot import " + stagedName + ": " + problem + ".");
            return false;
        }

        File staged = new File(stagingFolder, stagedName);
        File target = new File(storageFolder, stagedName);
        staged.renameTo(target);

        saveFileTimes(stagedName, getCurrentTime(), getCurrentTime());
        System.out.println("Imported " + stagedName + " (" + formatSize(target.length() / 1024.0) + ").");
        return true;
    }

    //deletes a file from Validation without importing it
    public boolean discardStaged(String stagedName) {
        if (!isPoweredOn) {
            System.out.println("The computer is off.");
            return false;
        }

        new File(stagingFolder, stagedName).delete();

        System.out.println("Discarded " + stagedName + ".");
        return true;
    }

    //report

    public void printComputerReport() {
        ArrayList<FileData> files = listAllFiles();

        System.out.println("----------------- COMPUTER REPORT -----------------");
        System.out.println(" Time     : " + getCurrentTime());
        System.out.println(" OS       : " + settings.getOsName());
        System.out.println(" Computer : " + settings.getComputerName());
        System.out.println(" Power    : " + (isPoweredOn ? "ON" : "OFF"));
        System.out.println(" Storage  : " + getStorageSpaceData());
        System.out.println(" Files    : " + files.size());
        System.out.println(" Staged   : " + listStaging().size() + " in Validation");
        System.out.println("-----------------------------------------------------");
    }

    public String getCurrentTime() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }


    //notes.txt returns "txt", returns "" when there is no extension behind file name
    //a name is usable when it is in the list of supported file types
    //this is essential to determine if a file type is valid
    public static boolean isValidName(String fullName) {
        return !extensionOf(fullName).isEmpty();
    }

    public boolean isWritableType(String fileType) {
        return settings.getWritableFileTypes().contains(fileType);
    }

    public boolean isReadableType(String fileType) {
        return settings.getReadableFileTypes().contains(fileType);
    }

    public static boolean isTextType(String fileType) {
        for (String type : TEXT_TYPES) {
            if (type.equals(fileType)) {
                return true;
            }
        }
        return false;
    }

    //opens a file with the program windows uses for that type, an mp3 starts playing
    public boolean openExternally(File file) {
        try {
            Desktop.getDesktop().open(file);
            return true;
        } catch (Exception e) {
            System.out.println("Could not open " + file.getName() + " on this computer.");
            return false;
        }
    }

    //opens a text file in the default editor so it can be typed in by hand
    public boolean openForEditing(File file) {
        try {
            Desktop.getDesktop().edit(file);
            return true;
        } catch (Exception e) {
            return openExternally(file);
        }
    }

    //adds filecreation time and file modification time into filetimes as data storage
    public void saveFileTimes(String fullName, String added, String modified) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(dictFile, true));
            writer.println(fullName + "|" + added + "|" + modified);
            writer.close();
        } catch (Exception e) {
            System.out.println("error trying to update the filetimes");
        }
    }
}
