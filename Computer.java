import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDateTime; //changes
import java.util.Scanner;

public class Computer {

    private static final String STORAGE_DIR = "Storage";
    private static final String STAGING_DIR = "Validation";

    //dictionary to store the creation date of the files since no way to access it through system
    private static final String DICT_FILE = "filetimes.txt";

    //supported text type that is not displayed in binary like for example mp3 files
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
            readableFileTypes = splitTypes("txt,md,log,json,csv,mp3,wav");
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

                    if (line.isEmpty() || line.equals("{") || line.equals("}")) {
                        continue;
                    }

                    int colon = line.indexOf(':');

                    if (colon == -1) {
                        continue;
                    }

                    String key = clean(line.substring(0, colon));
                    String value = clean(line.substring(colon + 1));
                    settings.apply(key, value);
                }

                reader.close();
            } catch (Exception e) {
                return new Settings();
            }

            return settings;
        }

        //removes quotes and the trailing comma from one value in the settings used to extract data
        public static String clean(String text) {
            text = text.replace("\"", "").trim();

            if (text.endsWith(",")) {
                text = text.substring(0, text.length() - 1);
            }
            return text;
        }

        public void apply(String key, String value) {
            try {
                if (key.equals("osName")) {osName = value;}
                    
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
                // not a number where a number was expected - keep the default
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
        //..................

    }


}


    // filedata here
