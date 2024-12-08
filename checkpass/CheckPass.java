package checkpass;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.InputStream;

public class CheckPass {
    public static void main(String[] args) {
        String zipPath = "files\\test.zip";
        String pass = "abcd";
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            zipFile.setPassword(pass.toCharArray());
            FileHeader fileHeader = zipFile.getFileHeaders().get(0);


            try {
                InputStream inputStream = zipFile.getInputStream(fileHeader);
                int firstByte = inputStream.read();
                if (firstByte != -1) {
                    System.out.println("Mật khẩu đúng là: " + pass);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } catch (ZipException e) {
            System.out.println(e.getMessage());
        }
    }
}
