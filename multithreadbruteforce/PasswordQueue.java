package multithreadbruteforce;

import java.util.concurrent.BlockingQueue;

public class PasswordQueue extends Thread {
    public static final int MAX_SIZE = 100000;
    private final Object lock = new Object();
    public BlockingQueue<String> queue;
    public static long index = 0;
    private boolean pause = false;


    public PasswordQueue() {
    }

    @Override
    public void run() {
        char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
//        char[] charset = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        char[] numbers = "0123456789".toCharArray();
        char[] lowerCase = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] specialChars = "!@#$%^&*()_+[]{}|;:,.<>?".toCharArray();

        long curIndex = 0;

//        for (char c1 : charset) {
//            for (char c2 : charset) {
//                for (char c3 : charset) {
//                    for (char c4 : charset) {
//                        curIndex++;
//                        if (curIndex <= index) {
//                            continue;
//                        }
//                        synchronized (lock) {
//                            while (pause) {
//                                try {
//                                    lock.wait();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                        if (queue.size() >= MAX_SIZE) {
//                            pauseThread();
//                        }
//                        String password = "" + c1 + c2 + c3 + c4;
////                        String password = "" + c1 + c2 + c3;
//
//                    try {
//                            queue.put(password);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }

        for (char c1 : charset) {
            for (char c2 : charset) {
                for (char c3 : charset) {

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    curIndex++;
                    if (curIndex <= index) {
                        continue;
                    }
                    synchronized (lock) {
                        while (pause) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (queue.size() >= MAX_SIZE) {
                        pauseThread();
                    }
                    String password = "" + c1 + c2 + c3;
                    try {
                        queue.put(password);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
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