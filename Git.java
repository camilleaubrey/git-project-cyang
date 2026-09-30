import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;


public class Git {

    public static boolean init() {
        int fileExistsCount = 0;
        File gitDirectory = new File("git");
        if (gitDirectory.exists() == false) {
            gitDirectory.mkdir();

        } else {
            fileExistsCount+=1;
        }
        File objDirectory = new File("git/objects");
        if (objDirectory.exists() == false) {
            objDirectory.mkdir();
        } else {
            fileExistsCount+=1;
        }
        File indexFile = new File("git/INDEX");
        if (indexFile.exists() == false) {
            try {
                indexFile.createNewFile();   
            } catch (IOException e) {
                System.out.println("cant");
            }
        } else {
            fileExistsCount+=1;
        }
        File headFile = new File("git/HEAD");
        if (headFile.exists() == false) {
            try {
                headFile.createNewFile();   
            } catch (IOException e) {
                System.out.println("cant");
            }
        } else {
            fileExistsCount+=1;
        }

        if (fileExistsCount == 4) {
            System.out.println("Git Repository Already Exists");
            return true;
        } else {
            System.out.println("Git Repository Created");
            return false;
        }
    }

    public static String hashFile(String filePath) throws IOException {
        String fileContents = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            fileContents = reader.readAllAsString();
        } catch (IOException e) {
            System.out.println("cant ");
        }
        try {
            MessageDigest sha1digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = sha1digest.digest(fileContents.getBytes());
            StringBuilder hashHexString = new StringBuilder();
                for (byte b : hashBytes) {
                    hashHexString.append(String.format("%02x", b));
                }
            return hashHexString.toString();
        } catch (Exception e) {
            System.out.println("cant");
            return null;
        }
    }

    public static void blob(String filePath) throws IOException {
        String fileName = hashFile(filePath);
        String newPath = "git/objects/" + fileName;
        File objectFile = new File(newPath);
        objectFile.createNewFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            try (FileWriter writer = new FileWriter(newPath)) {
                String fileContents = reader.readAllAsString();
                writer.write(fileContents);
                reader.close();
                writer.close();
            } catch (Exception e) {
                System.out.println("cant");
            }
        } catch (Exception e) {
                System.out.println("cant ");
        }
    }

    public static void index(String filePath) throws IOException {
        String fileHash = hashFile(filePath);
        String existingIndexContents = "";
        try (BufferedReader reader = new BufferedReader(new FileReader("git/INDEX"))) {
                existingIndexContents = reader.readAllAsString();
        }
        try (FileWriter writer = new FileWriter("git/INDEX")) {
            String indexEntry = fileHash + " " + filePath + "\n";
            writer.write(existingIndexContents);
            writer.append(indexEntry);
        }
    }

    public static void add(String filePath) throws IOException { 
        blob(filePath);
        index(filePath);
    }

    
}