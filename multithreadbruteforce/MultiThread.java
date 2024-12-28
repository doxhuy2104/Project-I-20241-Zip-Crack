package multithreadbruteforce;

import javafx.concurrent.Worker;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MultiThread extends Thread {
    private static Main mainApp;
    public static double startTime;
    public static double time;

    public static void startCracking(String zipFilePath, int numThreads, Main app, String charset, int maxPasswordLength) {
        updateNumThreads(Thread.activeCount() + "");
        mainApp = app;
        BlockingQueue<String> passwordQueue = new LinkedBlockingQueue<>(PasswordQueue.MAX_SIZE);

        PasswordQueue passwordGenerator = new PasswordQueue(charset.toCharArray(), maxPasswordLength);
        passwordGenerator.queue = passwordQueue;
        if (mainApp.comboBox.getValue() == "Thử từ đầu") {
            passwordGenerator.index = 0;
            mainApp.comboBox.setValue("Tiếp tục từ lần thử trước");
        } else {
            passwordGenerator.index = getIndex();
        }
//        passwordGenerator.index = getIndex();
        passwordGenerator.start();

        startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            CheckPass checkPass = new CheckPass(passwordQueue, passwordGenerator, zipFilePath);
            Thread thread = new Thread(checkPass);
            thread.start();
        }
    }


    public static void updateStatus(String status) {
        if (mainApp != null) {
            mainApp.updateStatus(status);
        }
    }

    private static void updateNumThreads(String numThreads) {
        if (mainApp != null) {
            mainApp.updateThreadStatus(numThreads);
        }
    }

    //Đọc index từ file
    public static long getIndex() {
        File indexFile = new File("files\\index.txt");
        if (!indexFile.exists()) {
            return 0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("files\\index.txt"))) {
            long index = 0;
            if (reader.ready()) {
                index = Long.parseLong(reader.readLine());
            }
            return index;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getCurrentIndex() {
        return PasswordQueue.getCurrentIndex();
    }

}