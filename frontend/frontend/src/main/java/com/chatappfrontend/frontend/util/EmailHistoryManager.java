package com.chatappfrontend.frontend.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EmailHistoryManager {
    private static final Path FILE_PATH = Path.of(System.getProperty("user.home"), ".bpchat", "email_history.txt");

    public static List<String> getEmails(){
        try {
            if(!Files.exists(FILE_PATH)){
                return new ArrayList<>();
            }

            return Files.readAllLines(FILE_PATH);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void addEmail(String email){
        try {
            Files.createDirectories(FILE_PATH.getParent());

            List<String> existing = getEmails();

            if(existing.contains(email)){
                return;
            }

            List<String> updated = new ArrayList<>();

            updated.add(email);
            updated.addAll(existing);

            Files.write(FILE_PATH, updated);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteEmail(String email){
        try {
            List<String> emails = getEmails();

            emails.remove(email);

            Files.write(FILE_PATH, emails);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}