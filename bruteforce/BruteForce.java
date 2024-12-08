package bruteforce;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BruteForce {
    public static void main(String[] args) {
        String zipPath = "files\\test.zip";
        List<String> passwords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("files\\passwords.txt"))) {
            String pass;
            while ((pass = reader.readLine()) != null) {
                passwords.add(pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ZipFile zipFile = new ZipFile(zipPath);
        try {
            FileHeader fileHeader = zipFile.getFileHeaders().get(0);
            double start = System.currentTimeMillis();

            for (String pass : passwords) {
                //System.out.println(pass);
                zipFile.setPassword(pass.toCharArray());
                try {
                    InputStream inputStream = zipFile.getInputStream(fileHeader);
                    int firstByte = inputStream.read();
                    if (firstByte != -1) {
                        zipFile.extractAll("files\\extracted");
                        File extractedFile = new File("files\\extracted");
                        if (!extractedFile.exists()) {
                            System.out.println("Mật khẩu đúng là: " + pass);
                            break;
                        }

                    }
                } catch (Exception e) {
                }
            }
            double end = System.currentTimeMillis();
            System.out.println("Thời gian thực hiện là: " + (end - start) / 1000);
        } catch (ZipException e) {

        }


    }
}
