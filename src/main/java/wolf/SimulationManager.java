package wolf;

import javafx.scene.paint.Color;
import java.io.*;
import java.util.*;

public class SimulationManager {

    // Сохранить симуляцию в файл
    public static boolean saveToFile(File file, HashSet<Cell> alive, int generation, int cols, int rows) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("#LifeGame simulation");
            writer.println("#cols=" + cols);
            writer.println("#rows=" + rows);
            writer.println("#generation=" + generation);
            for (Cell cell : alive) {
                Color c = cell.getColor();
                int r = (int) Math.round(c.getRed() * 255);
                int g = (int) Math.round(c.getGreen() * 255);
                int b = (int) Math.round(c.getBlue() * 255);
                writer.printf("%d %d %d %d %d%n", cell.getX(), cell.getY(), r, g, b);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Загрузить симуляцию без проверки размеров — возвращает полные данные
    public static FullSimulationData loadFromFileFull(File file) {
        if (file == null) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int loadedCols = -1, loadedRows = -1;
            int loadedGen = 0;
            List<Cell> loadedCells = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    if (line.startsWith("#cols=")) loadedCols = Integer.parseInt(line.substring(6));
                    else if (line.startsWith("#rows=")) loadedRows = Integer.parseInt(line.substring(6));
                    else if (line.startsWith("#generation=")) loadedGen = Integer.parseInt(line.substring(12));
                    continue;
                }
                if (line.isEmpty()) continue;
                String[] parts = line.split(" ");
                if (parts.length >= 5) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int r = Integer.parseInt(parts[2]);
                    int g = Integer.parseInt(parts[3]);
                    int b = Integer.parseInt(parts[4]);
                    loadedCells.add(new Cell(x, y, Color.rgb(r, g, b)));
                }
            }
            if (loadedCols == -1 || loadedRows == -1) return null;
            HashSet<Cell> newAlive = new HashSet<>(loadedCells);
            return new FullSimulationData(loadedCols, loadedRows, loadedGen, newAlive);
        } catch (Exception e) {
            return null;
        }
    }

    public static class FullSimulationData {
        public final int cols;
        public final int rows;
        public final int generation;
        public final HashSet<Cell> alive;
        public FullSimulationData(int cols, int rows, int generation, HashSet<Cell> alive) {
            this.cols = cols;
            this.rows = rows;
            this.generation = generation;
            this.alive = alive;
        }
    }
}