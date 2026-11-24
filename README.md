# CSA_project

Structure：
# 1:
**We extract the settings from config.json and then parse them into the correct formats to be usable inside the code**

# 2:
**File data class to extract the file properties in all forms. We extract modification date and creation date from the "filetimes.txt" file which is**
**a dictionary that contains the file data for all of the currently available files stored inside the folder Storage**

To save a file / create a file into storage, everytime a file is created, it gets firstly created inside "Validation" file to be then
scanned and verified if there is enough disk space to fit it into the main storage system. If it's enough, move the file in Validation into Storage, which is the official area for the main disk space.

**The good thing about validating the files before putting them inside storage is that we CAN drag our own files (Like an mp3 file from our own pc env) and if theres enough space in the pseudo disk, the mp3 file can actually be stored inside our emulator and can be opened via commands!**

In a nutshell, this system is to prevent you adding more files into the storage space when the code is not running and the file put inside results in passing the max storage threshold.

# 3: 

**For the computer class, simple functions like opening computer, check battery usage, etc functions lie in here.**

--------------------------------------------
**Command interpretation can lie in computer class or inside main method, just follow along ig**
