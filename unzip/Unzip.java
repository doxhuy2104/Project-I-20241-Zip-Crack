package unzip;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Unzip {
    public static void main(String[] args) {
        String src = "files\\test.zip";
        String des = "files";
        String pass = "1234";

        try {
            ZipFile zipFile = new ZipFile(src);
            zipFile.setPassword(pass.toCharArray());

            zipFile.extractAll(des);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
