package multithreadbruteforce;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static multithreadbruteforce.MultiThread.startTime;

public class CheckPass extends Thread {
    public final String zipPath = "files\\test.zip";
    private final List<String> passwords;

    public CheckPass(List<String> passwords) {
        this.passwords = passwords;
    }

    @Override
    public void run() {
        ZipFile zipFile = new ZipFile(zipPath);
        try {
            FileHeader fileHeader = zipFile.getFileHeaders().getFirst();
            while (!passwords.isEmpty()) {
                String pass = passwords.removeFirst();
                zipFile.setPassword(pass.toCharArray());

                try {
                    InputStream inputStream = zipFile.getInputStream(fileHeader);

                    int firstByte = inputStream.read();

                    if (firstByte != -1) {
                        zipFile.extractAll("files\\extracted");
                        File extractedFile = new File("files\\extracted");
                        if (extractedFile.exists()) {
                            System.out.println("Mật khẩu đúng là: " + pass);
                            long endTime = System.currentTimeMillis();

                            System.out.println("Thời gian thực hiện: " + (endTime - startTime) / 1000 + " giây");
                            System.exit(0);
                        }
                    }
                } catch (ZipException e) {
//                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();

        }
    }
}
