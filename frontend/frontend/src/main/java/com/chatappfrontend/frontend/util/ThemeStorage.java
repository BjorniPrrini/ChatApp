package com.chatappfrontend.frontend.util;

import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ThemeStorage {
    private static final Path FILE_PATH = Path.of(System.getProperty("user.home"), ".bpchat", "theme.properties");

    public static void save(String accent, String text, String panel, double opacity){
        try {
            Files.createDirectories(FILE_PATH.getParent());

            Properties properties = new Properties();

            properties.setProperty("glow.color", accent);
            properties.setProperty("text.color", text);
            properties.setProperty("panel.color", panel);
            properties.setProperty("panel.opacity", String.valueOf(opacity));

            try (OutputStream output = Files.newOutputStream(FILE_PATH)) {
                properties.store(output, "BP-Chat theme settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties load(){
        Properties properties = new Properties();

        if(!Files.exists(FILE_PATH)){
            return properties;
        }

        try (InputStream input = Files.newInputStream(FILE_PATH)) {
            properties.load(input);

            return properties;
        } catch (IOException e) {
            e.printStackTrace();

            return properties;
        }
    }

    public static void applyTheme(Scene scene){
        Properties properties = load();

        String accent = properties.getProperty("glow.color", AppConfig.get("glow.color"));
        String text = properties.getProperty("text.color", AppConfig.get("text.color"));
        String panel = properties.getProperty("panel.color", AppConfig.get("panel.color"));

        double opacity = Double.parseDouble(properties.getProperty("panel.opacity", AppConfig.get("panel.opacity")));

        Color accentColor = Color.web(accent);
        Color textColor = Color.web(text);
        Color panelColor = Color.web(panel);

        Color accentDim = accentColor.deriveColor(0, 1.0, 0.8, 1.0);

        String accentRgb = toRgbString(accentColor);
        String accentDimColor = toRgbString(accentDim);
        String accentWash = toRgbaString(accentColor, 0.10);
        String accentGlow = toRgbaString(accentColor, 0.35);
        String textRgb = toRgbString(textColor);
        String panelRgba = toRgbaString(panelColor, opacity);

        scene.getRoot().setStyle("-app-accent: " + accentRgb + "; -app-accent-dim: " + accentDimColor + "; -app-accent-wash: " + accentWash + "; -app-accent-glow: " + accentGlow + "; -app-text: " + textRgb + "; -app-bg-panel: " + panelRgba + ";");
    }

    public static void applyTheme(Scene scene, Color accent, Color panel, Color text, double opacity){
        if(scene == null){
            return;
        }

        Color accentDim = accent.deriveColor(0, 1.0, 0.8, 1.0);

        String accentRgb = toRgbString(accent);
        String accentDimColor = toRgbString(accentDim);
        String accentWash = toRgbaString(accent, 0.10);
        String accentGlow = toRgbaString(accent, 0.35);
        String textRgb = toRgbString(text);
        String panelRgba = toRgbaString(panel, opacity);

        scene.getRoot().setStyle("-app-accent: " + accentRgb + "; -app-accent-dim: " + accentDimColor + "; -app-accent-wash: " + accentWash + "; -app-accent-glow: " + accentGlow + "; -app-text: " + textRgb + "; -app-bg-panel: " + panelRgba + ";");
    }

    private static String toRgbString(Color color){
        return String.format("#%02x%02x%02x", (int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255));
    }

    private static String toRgbaString(Color color, double opacity){
        return String.format("rgba(%d, %d, %d, %.2f)", (int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255), opacity);
    }
}