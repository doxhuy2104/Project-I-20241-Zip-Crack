package multithreadbruteforce;

import javax.swing.plaf.basic.BasicButtonUI;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MultiThread {
    public static long startTime;

    public static void main(String[] args) {
        BlockingQueue<String> passwordQueue = new LinkedBlockingQueue<>(PasswordQueue.MAX_SIZE);

        PasswordQueue passwordGenerator = new PasswordQueue();
        passwordGenerator.queue = passwordQueue;

        passwordGenerator.index = getIndex();
        passwordGenerator.start();

        int numThreads = 8;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            CheckPass checkPass = new CheckPass(passwordQueue, passwordGenerator);
            checkPass.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
                writer.write(passwordGenerator.index + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private static long getIndex() {
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
}
