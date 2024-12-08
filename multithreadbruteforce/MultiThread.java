package multithreadbruteforce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MultiThread {
    public static long startTime;

    public static void main(String[] args) {

        List<String> passwords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("files\\passwords.txt"))) {
            String pass;
            while ((pass = reader.readLine()) != null) {
                passwords.add(pass);
            }
        } catch (Exception e) {
        }

        int numThreads = 8;
        int partSize = passwords.size() / numThreads;


        startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            int start = i * partSize;
            int end = start + partSize;
            if (i == numThreads - 1) {
                end = passwords.size();
            }
            List<String> part = new ArrayList<>(passwords.subList(start, end));
            CheckPass checkPass = new CheckPass(part);
            checkPass.start();
        }
    }
}
