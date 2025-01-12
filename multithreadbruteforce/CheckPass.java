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


            while (!passwordFound && !Thread.currentThread().isInterrupted()) {
                try {
                    while (!isRunning && !Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
//                        synchronized (this) {
//                            wait();
//                        }
                    }

                    if (passwordGenerator.isFinished && passwordQueue.isEmpty()) {
                        isRunning = false;
                        Main mainApp = Main.getMainApp();
                        resetIndex();
                        synchronized (passwordGenerator) {
                            passwordGenerator.index = 0;
                        }
                        mainApp.time += System.currentTimeMillis() - mainApp.startTime;
                        mainApp.updateButton();
                        mainApp.updateStatus("Không tìm thấy mật khẩu");
                        mainApp.updateTimeLabel("Thời gian thực hiện: " + mainApp.time / 1000 + "s");
                        mainApp.updateProgress(1);
                        mainApp.setControlsDisabled(false);
                        mainApp.disableComboBox(true);
                        mainApp.started = false;
                        mainApp.time = 0;
                        mainApp.stopAllThreads();
                        return;
                    }
                    String pass = passwordQueue.take();
                    synchronized (passwordGenerator) {

                        passwordGenerator.index++;
                        if (passwordGenerator.index % 100 == 0) {
                            double progress = (double) passwordGenerator.index / passwordGenerator.totalPasswords;
                            Main.getMainApp().updateProgress(progress);
//                            Main.getMainApp().updateStatus("Đang kiểm tra mật khẩu: " + pass);
                        }
                    }

                    zipFile.setPassword(pass.toCharArray());
                    //Thử đọc 1 byte từ file
                    try (InputStream inputStream = zipFile.getInputStream(fileHeader)) {
                        if (inputStream.read() != -1) {
                            zipFile.extractAll("files\\extracted");
                            File extractedFile = new File("files\\extracted\\" + fileHeader.getFileName());
                            if (extractedFile.exists()) {
                                cleanupExtracted();
                                endTime = System.currentTimeMillis();
                                Main mainApp = Main.getMainApp();

                                mainApp.time += endTime - mainApp.startTime;
                                passwordFound = true;
                                resetIndex();
                                mainApp.updateStatus("Đã tìm thấy mật khẩu: " + pass);
                                mainApp.updateTimeLabel("Trong thời gian: " + mainApp.time / 1000 + "s");
//                                mainApp.foundedPopup(pass, mainApp.time / 1000);
                                mainApp.time = 0;
                                mainApp.isRunning = false;
                                mainApp.updateButton();
                                mainApp.setControlsDisabled(false);
                                mainApp.updateProgress(1);
                                mainApp.disableComboBox(true);
                                mainApp.started = false;
                                mainApp.stopAllThreads();
                                return;
                            }
                        }
                    } catch (Exception e) {
                    }

                    if (passwordQueue.isEmpty()) {
                        passwordGenerator.resumeThread();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (ZipException e) {
        }
    }

    private void cleanupExtracted() {
        File extractedFile = new File("files\\extracted");
        if (extractedFile.exists()) {
            deleteDirectory(extractedFile);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private static void resetIndex() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
            writer.write(0 + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}