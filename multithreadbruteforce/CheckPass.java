package multithreadbruteforce;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

public class CheckPass extends Thread {
    private final BlockingQueue<String> passwordQueue;
    private final PasswordQueue passwordGenerator;
    private final String zipPath;
    public static volatile boolean passwordFound = false;
    public static long endTime;

    public static volatile boolean isRunning = true;

    public CheckPass(BlockingQueue<String> passwordQueue, PasswordQueue passwordGenerator, String zipPath) {
        this.passwordQueue = passwordQueue;
        this.passwordGenerator = passwordGenerator;
        this.zipPath = zipPath;
    }


    @Override
    public void run() {
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            FileHeader fileHeader = zipFile.getFileHeaders().getFirst();


            while (!passwordFound) {
                try {
                    while (!isRunning) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String pass = passwordQueue.take();
//                    MultiThread.updateStatus(Thread.currentThread().getName() + " Checking: " + pass);
                    synchronized (passwordGenerator) {
                        passwordGenerator.index++;
                    }

                    zipFile.setPassword(pass.toCharArray());
                    //Thử đọc 1 byte từ file
                    try (InputStream inputStream = zipFile.getInputStream(fileHeader)) {
                        if (inputStream.read() != -1) {
                            zipFile.extractAll("files\\extracted");
                            File extractedFile = new File("files\\extracted\\" + fileHeader.getFileName());
                            if (extractedFile.exists()) {
                                endTime = System.currentTimeMillis();
                                MultiThread.time += endTime - MultiThread.startTime;
                                passwordFound = true;
                                resetIndex();
                                MultiThread.updateStatus("Đã tìm thấy mật khẩu: " + pass + " trong " + MultiThread.time / 1000 + "s");
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    } catch (Exception e) {
                    }

                    if (passwordQueue.isEmpty()) {
                        passwordGenerator.resumeThread();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (ZipException e) {
        }
    }

    private static void resetIndex() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
            writer.write(0 + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        File extractedFile = new File("files\\extracted");
        if (extractedFile.exists()) {
            extractedFile.delete();
        }
    }
}