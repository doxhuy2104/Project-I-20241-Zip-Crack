package multithreadbruteforce;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import static multithreadbruteforce.MultiThread.startTime;

public class CheckPass extends Thread {
    public final String zipPath = "files\\test.zip";
    private final BlockingQueue<String> passwordQueue;
    private final PasswordQueue passwordGenerator;

    public CheckPass(BlockingQueue<String> passwordQueue, PasswordQueue passwordGenerator) {
        this.passwordQueue = passwordQueue;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public void run() {
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            FileHeader fileHeader = zipFile.getFileHeaders().getFirst();

            while (true) {
                try {
                    String pass = passwordQueue.take();

                    synchronized (passwordGenerator) {
                        passwordGenerator.index++;
                    }

//                    double startTime = System.nanoTime();
                    zipFile.setPassword(pass.toCharArray());
                    try (InputStream inputStream = zipFile.getInputStream(fileHeader)) {
                        if (inputStream.read() != -1) {
                            File extractedFile = new File("files\\extracted");
                            if (extractedFile.exists()) {
                                System.out.println("Mật khẩu đúng là: " + pass);
                                removeIndex();

                                long endTime = System.currentTimeMillis();
                                System.out.println("Thời gian thực hiện: " + (endTime - startTime) / 1000 + " giây");
                                System.exit(0);
                            }
                        }
                    } catch (Exception e) {
                    }

//                    double endTime = System.nanoTime();
//                    System.out.println("Password: " + pass+" "+(endTime-startTime)+" ns");

                    if (passwordQueue.isEmpty()) {
                        passwordGenerator.resumeThread();
                    }
                } catch (InterruptedException e) {
                }
            }
        } catch (ZipException e) {
        }
    }


    void removeIndex() {
        File indexFile = new File("files\\index.txt");
        indexFile.delete();
        File extracted = new File("files\\extracted");
        File files[] = extracted.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
