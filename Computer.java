// import java.time.LocalDateTime;
import java.util.ArrayList;

public class Computer {
    // Attributes to the computer
    private String computerName;
    private String osName;
    private boolean isPoweredOn;
    private int batteryLevel;
    private double maxStorageGB;
    private double usedStorageGB;
    private ArrayList<FileData> storedFiles;
    
    private ArrayList<String> support_file_types; 

    // File class object used to store metadata and the content of the files
    public static class FileData {
        private String fileName;      
        private String fileType;      
        private String creationDate;  
        private String lastModified;  
        private double sizeGB;        
        private String content;       
        
        // Initializing file with default val
        public FileData(String name, String type, String date) {
            fileName = name;
            fileType = type;
            creationDate = date;
            lastModified = date;
            sizeGB = 0.0;
            content = "";
        }
    }
}