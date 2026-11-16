import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Computer {

    // attributes here
    private String computerName;
    private String osName;
    private boolean isPoweredOn;
    private int batteryLevel;
    private double maxStorageKB;   
    private double usedStorageKB; 
    
    private ArrayList<FileData> storedFiles;
    private ArrayList<String> supported_file_types; 

    // filedata class which stores metadata 
    public static class FileData {
        private String fileName;     
        private String fileType;     
        private String creationDate; 
        private String lastModified;
        private double sizeKB;      
        private String content; // Just store content within the files metadata, it is simpler to retrieve file info that way

        // constructor for the files
        public FileData(String name, String type, String date) {
            fileName = name;
            fileType = type;
            creationDate = date;
            lastModified = date;
            sizeKB = 0.0;
            content = "";
        }

        // methods here for the file itself
        public String getFileName(){
            return fileName; 
        }

        public String getFileType(){
            return fileType; 
        
        }

        public String getCreationDate() {
            return creationDate; 
        }

        public String getLastModified() {
            return lastModified;
        }

        public double getSizeKB() {
            return sizeKB;
        }

        public String getContent() {
            return content;
        }

        public void writeContent(String text, String modifyDate) { //overwrites content of the file every single time used, cant edit it directly tho :(
            content = text;
            lastModified = modifyDate;

            if (text.isEmpty()) {
                this.sizeKB = 0.0;
            } else {
                this.sizeKB = text.length() / 1024.0;
            }
        }
        
        public String fileProperties() {
            return String.format("File: %s / Type: %s / Created: %s / Modified: %s / Size: %.2f KB", fileName, fileType, creationDate, lastModified, sizeKB);
        }
    }

    // constructor for the main computer
    public Computer(String name, String os) {
        computerName = name;
        osName = os;
        isPoweredOn = false;
        batteryLevel = 100;
        maxStorageKB = 100.0; 
        usedStorageKB = 0.0;
        
        storedFiles = new ArrayList<>();
        
        supported_file_types = new ArrayList<>();
        // add supported file types here, add it anytime it wont break anything just think of file types
        supported_file_types.add("txt");
        supported_file_types.add("md");
    }

    // methods here
    public boolean isPoweredOn() {
        return isPoweredOn;
    }

    public void togglePower() {
        if (isPoweredOn) {
            System.out.println("Shutting down computer...");
            isPoweredOn = false;
        } else {
            System.out.println("Booting up " + computerName + "...");
            isPoweredOn = true;
        }
    }

    public int checkBatteryStatus() {
        return batteryLevel;
    }

    // creates a file object and adds it into the stored files list as a storage space. returns false if creation fails and vice versa
    public boolean createFile(String fullFileName) {
        if (!isPoweredOn) {
            System.out.println("System is off, please open the computer to do any actions.");
            return false;
        }

        // extracts the file types (Eg: .txt, .md) to check if its in the supported list
        String fileTypeExtract = "";
        int dotIndex = fullFileName.indexOf(".");

        if (dotIndex == -1 || dotIndex == fullFileName.length() - 1) {
            System.out.println("Error: Filename must have a file type (Eg: .txt)");
            return false;
        }
        
        fileTypeExtract = fullFileName.substring(dotIndex + 1);

        if (!supported_file_types.contains(fileTypeExtract)) {
            System.out.println("Error: Unsupported file type '" + fileTypeExtract + "'. Allowed file types: " + supported_file_types);
            return false;
        }

        // checks if there are duplicates

        return;
    }
}