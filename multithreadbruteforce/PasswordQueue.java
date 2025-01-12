package multithreadbruteforce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.BlockingQueue;

public class PasswordQueue extends Thread {
    public static final int MAX_SIZE = 100000;
    private final Object lock = new Object();
    public BlockingQueue<String> queue;
    public static volatile long index = 0;
    private boolean pause = false;
    private char[] charset;
    private int maxPasswordLength;
    public static boolean isRunning = true;
    private volatile long curIndex = 0; // Thêm biến instance
    public long totalPasswords = 0;
    public boolean isFinished = false;
    public String tryMethod;

    public PasswordQueue(char[] charset, int maxPasswordLength, String tryMethod) {
        this.maxPasswordLength = maxPasswordLength;
        this.charset = charset;
        this.tryMethod = tryMethod;
        this.totalPasswords = tryMethod == "Brute Force" ? calculateTotalPassword() : getTotalDictionaryPasswords();
    }

    @Override
    public void run() {
        try {
            if (tryMethod.equals("Brute Force")) {
                for (int length = 1; length <= maxPasswordLength && !Thread.currentThread().isInterrupted(); length++) {
                    generatePasswords(new char[length], 0, length);
                }
            } else {
                dictionaryPasswords();
            }
            isFinished = true;
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generatePasswords(char[] currentPassword, int position, int maxLength) {
        if (!isRunning || Thread.currentThread().isInterrupted()) {
            return;
        }

        if (position == maxLength) {
            synchronized (lock) {
                try {
                    while (!isRunning && !Thread.currentThread().isInterrupted()) {
                        lock.wait();
                    }

                    curIndex++;
                    if (curIndex <= index) {
                        return;
                    }

                    while (pause && !Thread.currentThread().isInterrupted()) {
                        lock.wait();
                    }

                    if (queue.size() >= MAX_SIZE) {
                        pauseThread();
                    }

                    queue.put(new String(currentPassword));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            return;
        }

        for (char c : charset) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            currentPassword[position] = c;
            generatePasswords(currentPassword, position + 1, maxLength);
        }
    }

    private void dictionaryPasswords() {
        try (BufferedReader reader = new BufferedReader(new FileReader("files\\dictionary.txt"))) {
            String password;
            while ((password = reader.readLine()) != null) {
                synchronized (lock) {
                    try {
                        while (!isRunning) {
                            lock.wait();
                        }

                        curIndex++;
                        if (curIndex <= index) {
                            continue;
                        }

                        while (pause) {
                            lock.wait();
                        }

                        if (queue.size() >= MAX_SIZE) {
                            pauseThread();
                        }

                        queue.put(password);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public long getTotalDictionaryPasswords() {
        long total = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("files\\dictionary.txt"));
            String password;
            while ((password = reader.readLine()) != null) {
                total++;
            }
            reader.close();

        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        return total;
    }

    public long calculateTotalPassword() {
        long total = 0;
        for (int i = 1; i <= maxPasswordLength; i++) {
            total += Math.pow(charset.length, i);
        }
        return total;
    }

    public static long getCurrentIndex() {
        return index;
    }

    public void pauseThread() {
        synchronized (lock) {
            pause = true;
        }
    }

    public void resumeThread() {
        synchronized (lock) {
            pause = false;
            lock.notifyAll();
        }
    }
}
