package genpass;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PasswordGenerate {
    public static void main(String[] args) {
        char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\passwords.txt"))) {
            for (char c1 : charset) {
                for (char c2 : charset) {
                    for (char c3 : charset) {
                        for (char c4 : charset) {
                            writer.write(c1 + "" + c2 + "" + c3 + "" + c4 + "\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
