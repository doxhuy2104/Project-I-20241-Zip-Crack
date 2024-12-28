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

    public PasswordQueue(char[] charset, int maxPasswordLength) {
        this.maxPasswordLength = maxPasswordLength;
        this.charset = charset;
    }

    @Override
    public void run() {
        for (int length = 1; length <= maxPasswordLength; length++) {
            generatePasswords(new char[length], 0, length);
        }
    }

    private void generatePasswords(char[] currentPassword, int position, int maxLength) {
        if (!isRunning) {
            return;
        }

        if (position == maxLength) {
            synchronized (lock) {
                while (!isRunning) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                        return;
                    }
                }

                curIndex++;
                if (curIndex <= index) {
                    return;
                }

                while (pause) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                        return;
                    }
                }

                if (queue.size() >= MAX_SIZE) {
                    pauseThread();
                }

                try {
                    queue.put(new String(currentPassword));
                } catch (InterruptedException e) {
                    return;
                }
            }
            return;
        }

        for (char c : charset) {
            currentPassword[position] = c;
            generatePasswords(currentPassword, position + 1, maxLength);
        }
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
