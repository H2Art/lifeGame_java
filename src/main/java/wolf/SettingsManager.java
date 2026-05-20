package wolf;

import javafx.scene.paint.Color;

import java.io.*;
import java.util.Properties;

public class SettingsManager {
    private static final String DEFAULT_SETTINGS_FILE = "config.properties";

    // Сохранить настройки в файл по умолчанию (автоматически)
    public static void saveDefault(int cols, int rows, int fps, int density, String gridColor) {
        Properties props = new Properties();
        props.setProperty("cols", String.valueOf(cols));
        props.setProperty("rows", String.valueOf(rows));
        props.setProperty("fps", String.valueOf(fps));
        props.setProperty("density", String.valueOf(density));
        props.setProperty("grid", gridColor);
        try (FileOutputStream out = new FileOutputStream(DEFAULT_SETTINGS_FILE)) {
            props.store(out, "Life Game settings (auto-saved)");
        } catch (IOException e) {
            System.err.println("Could not auto-save settings: " + e.getMessage());
        }
    }

    // Загрузить настройки из файла по умолчанию (если есть)
    public static int[] loadDefault() {
        File file = new File(DEFAULT_SETTINGS_FILE);
        if (!file.exists()) return null;
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            int cols = Integer.parseInt(props.getProperty("cols", "30"));
            int rows = Integer.parseInt(props.getProperty("rows", "30"));
            int fps = Integer.parseInt(props.getProperty("fps", "10"));
            int density = Integer.parseInt(props.getProperty("density", "20"));
            Color gridColor = getColor(props.getProperty("grid", "BLACK"));
            LifeGameUI.setColor(gridColor);
            return new int[]{cols, rows, fps, density};
        } catch (Exception e) {
            System.err.println("Could not load settings: " + e.getMessage());
            return null;
        }
    }
    // Конвертация строки в цвет
    private static Color getColor(String colorName) {
        if (colorName.equals("BLACK")) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }
}