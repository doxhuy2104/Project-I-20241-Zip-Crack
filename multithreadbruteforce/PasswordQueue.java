package multithreadbruteforce;

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

    public PasswordQueue(char[] charset, int maxPasswordLength) {
        this.maxPasswordLength = maxPasswordLength;
        this.charset = charset;
        this.totalPasswords = calculateTotalPassword();
    }

    @Override
    public void run() {
        try {
            for (int length = 1; length <= maxPasswordLength && !Thread.currentThread().isInterrupted(); length++) {
                generatePasswords(new char[length], 0, length);
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

    public long calculateTotalPassword() {
        long total = 0;
        for (int i = 1; i <= maxPasswordLength; i++) {
            total += Math.pow(charset.length, i);
        }
        return total;
    }

    public double getProgress() {
        return (double) curIndex / totalPasswords;
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
