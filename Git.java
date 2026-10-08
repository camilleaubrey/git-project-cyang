import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.security.MessageDigest;


public class Git {

    public static boolean init() {
        int fileExistsCount = 0;
        File gitDirectory = new File("git");
        if (gitDirectory.exists() == false) {
            gitDirectory.mkdir();

        } else {
            fileExistsCount += 1;
        }
        File objDirectory = new File("git/objects");
        if (objDirectory.exists() == false) {
            objDirectory.mkdir();
        } else {
            fileExistsCount += 1;
        }
        File indexFile = new File("git/INDEX");
        if (indexFile.exists() == false) {
            try {
                indexFile.createNewFile();
            } catch (IOException e) {
                System.out.println("cant make index");
            }
        } else {
            fileExistsCount += 1;
        }
        File headFile = new File("git/HEAD");
        if (headFile.exists() == false) {
            try {
                headFile.createNewFile();
            } catch (IOException e) {
                System.out.println("cant make HEAD");
            }
        } else {
            fileExistsCount += 1;
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
        File fp = new File(filePath);
        if (fp.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                fileContents = reader.readAllAsString();
            } catch (IOException e) {
                System.out.println("cant read all as string");
            }
        } else {
            fileContents = filePath;
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
            System.out.println("cant hash");
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
                System.out.println("blob: cant read and write files: " + e);
            }
        } catch (Exception e) {
            System.out.println("cant: " + e);
        }
    }

    public static void index(String filePath) throws IOException {
        String fileHash = hashFile(filePath);
        String existingIndexContents = "";

        Path sourcePath = Paths.get(filePath);
        String relativePath = sourcePath.toString();
        String indexEntry = fileHash + " " + relativePath;

        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("git/INDEX"))) {
            existingIndexContents = reader.readAllAsString();
        }

        try (FileWriter writer = new FileWriter("git/INDEX")) {
            String[] indexContentArray = existingIndexContents.split("\n");
            StringBuilder newIndexContent = new StringBuilder();
            for (int i = 0; i < indexContentArray.length; i++) {
                // checking if filePath already exists in the index folder
                int spaceIndex = indexContentArray[i].indexOf(" ");
                String currFilePath = indexContentArray[i].substring(spaceIndex + 1);

                // add same filePath, but with diff hash
                if (currFilePath.equals(filePath)) {
                    newIndexContent.append(indexEntry + "\n");
                    updated = true;
                }

                // add existing line (different filePath)
                else {
                    newIndexContent.append(indexContentArray[i] + "\n");
                }
            }

            // if filePath doesn't exist yet
            if (!updated) {
                newIndexContent.append(indexEntry);
            }

            // cut out "\n" in beginning and end
            String newIndexContentString =
                    newIndexContent.toString().trim();

            // write to index file with the new index contents
            writer.write(newIndexContentString);
        }
    }

    public static void add(String filePath) throws IOException {
        blob(filePath);
        index(filePath);
    }

    public static String createTree(ArrayList<String> workingList, String directoryPath) throws IOException {
        StringBuilder treeContents = new StringBuilder();

        for (String line : workingList) {
            // decomposing each entry into its type, hash, and file path
            String[] parts = line.split(" ", 3);

            String type = parts[0];
            String hash = parts[1];
            String filePath = parts[2];

            File file = new File(filePath);
            String parentDirectory = file.getParent();

            // checking if the file belongs directly inside the directory we're creating the tree for
            if (directoryPath.equals(parentDirectory)) {
                String fileName = file.getName();
                treeContents.append(type + " " + hash + " " + fileName + "\n");
            }
        }

        String treeHash = hashFile(treeContents.toString());
        FileWriter fw = new FileWriter("git/objects/" + treeHash);
        fw.write(treeContents.toString());
        fw.close();
        return treeHash;
    }

    public static ArrayList<String> workingList() throws IOException {
        ArrayList<String> workingList = new ArrayList<>();
        ArrayList<String> workingListFiles = new ArrayList<>();
        HashMap<String, String> fileMap = new HashMap<>();

        // reading the existing index file (going line by line), prefix "blob"
        try (BufferedReader br = new BufferedReader(new FileReader("git/INDEX"))) {
            String line = br.readLine();

            while (line != null) {
                // add same filePath, but with diff hash
                if (!line.isBlank()) {
                    workingList.add("blob " + line);
                }

                line = br.readLine();
            }
        }

        // get file paths and store original entries
        for (String line : workingList) {
            String[] parts = line.split(" ", 3);
            String filePath = parts[2];

            workingListFiles.add(filePath);
            fileMap.put(filePath, line);
        }

        // sort alphabetically
        Collections.sort(workingListFiles);

        // rebuild working list in sorted order
        ArrayList<String> sortedWorkingList = new ArrayList<>();

        for (String filePath : workingListFiles) {
            sortedWorkingList.add(fileMap.get(filePath));
        }

        return sortedWorkingList;
    }


}
