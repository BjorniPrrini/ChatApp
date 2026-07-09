package com.chatappfrontend.frontend.util;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/settings.properties")) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}