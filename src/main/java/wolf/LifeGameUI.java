package wolf;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.*;

public class LifeGameUI extends Application {

    private static final int MAX_BOARD_WIDTH = 1000;
    private static final int MAX_BOARD_HEIGHT = 800;
    private static final int UI_PANEL_WIDTH = 240; // ширина панели с настройками

    private int currentCols;
    private int currentRows;
    private int currentCellSize;

    private HashSet<Cell> alive;
    private GameBoard board;
    private Canvas canvas;
    private Label generationLabel;
    private int generation = 0;

    private AnimationTimer timer;
    private long lastUpdate = 0;
    private long frameInterval;

    private static CheckBox gridToggler;
    private Slider fpsSlider;
    private Label fpsLabel;
    private Slider colsSlider;
    private Label colsLabel;
    private Slider rowsSlider;
    private Label rowsLabel;
    private Label densityLabel;
    private int generationDensity = 20;

    private static Color gridColor = Color.BLACK;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        gridToggler = new CheckBox();
        // Загружаем настройки из config.properties (если есть)
        int[] settings = SettingsManager.loadDefault();
        if (settings != null) {
            currentCols = settings[0];
            currentRows = settings[1];
            generationDensity = settings[3];
        } else {
            currentCols = Constants.getWidthCells();
            currentRows = Constants.getHeightCells();
            generationDensity = 20;
        }
        currentCellSize = computeCellSize(currentCols, currentRows);

        alive = new HashSet<>();

        double playingAreaWidth = currentCols * currentCellSize;
        double playingAreaHeight = currentRows * currentCellSize;
        canvas = new Canvas(playingAreaWidth, playingAreaHeight);
        board = new GameBoard(canvas, currentCellSize, currentCols, currentRows);
        board.drawGrid(gridColor);

        frameInterval = 1_000_000_000 / (settings != null ? settings[2] : Constants.getFps());
        initTimer();

        // Создаём UI-панель (VBox) со всеми элементами
        VBox uiBox = new VBox(10);
        uiBox.setStyle("-fx-padding: 15; -fx-background-color: #e0e0e0; -fx-spacing: 12;");
        uiBox.setPrefWidth(UI_PANEL_WIDTH);
        uiBox.setAlignment(Pos.CENTER);

        // Кнопки (без настроек)
        Button nextButton = new Button("Next generation");
        Button resetButton = new Button("Clear");
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        Button randomButton = new Button("Random");
        Button applyButton = new Button("Apply (pauses simulation)");
        Button saveSimButton = new Button("Save simulation");
        Button loadSimButton = new Button("Load simulation");
        generationLabel = new Label("Generation: 0");

        // Чекбокс и его лейбл
        Label gridTogglerLabel = new Label("Toggle grid");

        // Слайдеры
        fpsSlider = new Slider(0, 50, (settings != null ? settings[2] : Constants.getFps()));
        fpsSlider.setShowTickLabels(true);
        fpsSlider.setMajorTickUnit(10);
        fpsLabel = new Label("FPS: " + (int) fpsSlider.getValue());
        fpsSlider.valueProperty().addListener((obs, old, val) ->
                fpsLabel.setText("FPS: " + val.intValue()));

        colsSlider = new Slider(3, 100, currentCols);
        colsSlider.setShowTickLabels(true);
        colsSlider.setMajorTickUnit(20);
        colsLabel = new Label("Width: " + currentCols);
        colsSlider.valueProperty().addListener((obs, old, val) ->
                colsLabel.setText("Width: " + val.intValue()));

        rowsSlider = new Slider(3, 100, currentRows);
        rowsSlider.setShowTickLabels(true);
        rowsSlider.setMajorTickUnit(20);
        rowsLabel = new Label("Height: " + currentRows);
        rowsSlider.valueProperty().addListener((obs, old, val) ->
                rowsLabel.setText("Height: " + val.intValue()));

        Slider densitySlider = new Slider(0, 100, generationDensity);
        densitySlider.setShowTickLabels(true);
        densitySlider.setMajorTickUnit(25);
        densityLabel = new Label("Density: " + generationDensity + "%");
        densitySlider.valueProperty().addListener((obs, old, val) -> {
            generationDensity = val.intValue();
            densityLabel.setText("Density: " + generationDensity + "%");
        });

        // Действия кнопок
        nextButton.setOnAction(e -> nextGeneration());
        resetButton.setOnAction(e -> resetGame());
        startButton.setOnAction(e -> timer.start());
        stopButton.setOnAction(e -> timer.stop());
        randomButton.setOnAction(e -> randomGeneration());
        applyButton.setOnAction(e -> applyChanges());
        saveSimButton.setOnAction(e -> saveSimulation());
        loadSimButton.setOnAction(e -> loadSimulation());
        canvas.setOnMouseClicked(e -> userInput(e.getX(), e.getY()));
        gridToggler.setOnAction(e -> changeGridColor());

        // Единая ширина кнопок
        List<Button> buttons = Arrays.asList(nextButton, resetButton, startButton, stopButton,
                randomButton, applyButton, saveSimButton, loadSimButton);
        for (Button btn : buttons) {
            btn.setPrefWidth(UI_PANEL_WIDTH - 30);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        uiBox.getChildren().addAll(
                nextButton, resetButton, startButton, stopButton, randomButton, applyButton,
                new Separator(),
                saveSimButton, loadSimButton,
                new Separator(),
                gridTogglerLabel,
                gridToggler,
                new Separator(),
                fpsLabel, fpsSlider,
                colsLabel, colsSlider,
                rowsLabel, rowsSlider,
                densityLabel, densitySlider,
                generationLabel
        );

        // Оборачиваем VBox в ScrollPane, чтобы можно было прокручивать
        ScrollPane scrollPane = new ScrollPane(uiBox);
        scrollPane.setFitToWidth(true);          // растягивать по ширине
        scrollPane.setPrefHeight(MAX_BOARD_HEIGHT); // ограничиваем высоту (будет полоса, если элементов много)
        scrollPane.setStyle("-fx-background: #e0e0e0; -fx-background-color: #e0e0e0;");

        HBox root = new HBox(canvas, scrollPane);
        Scene scene = new Scene(root, currentCols * currentCellSize + UI_PANEL_WIDTH, currentRows * currentCellSize);
        primaryStage.setTitle("Life Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Автоматическое сохранение настроек при закрытии
        primaryStage.setOnCloseRequest(e -> {
            SettingsManager.saveDefault(
                    currentCols, currentRows,
                    (int) fpsSlider.getValue(),
                    generationDensity,
                    getGridColorString()
            );
        });
    }

    // -------------------------------------------------------------
    // Применение настроек (Apply)
    // -------------------------------------------------------------
    private void applyChanges() {
        if (timer != null) timer.stop();
        int newFps = (int) fpsSlider.getValue();
        if (newFps == 0) {
            frameInterval = Long.MAX_VALUE;
        } else {
            frameInterval = 1_000_000_000 / newFps;
        }
        lastUpdate = 0;

        int newCols = (int) colsSlider.getValue();
        int newRows = (int) rowsSlider.getValue();
        boolean sizeChanged = (newCols != currentCols) || (newRows != currentRows);
        if (sizeChanged) {
            currentCols = newCols;
            currentRows = newRows;
            currentCellSize = computeCellSize(currentCols, currentRows);
            rebuildGameArea();
        }
    }

    // -------------------------------------------------------------
    // Перестроение игровой области
    // -------------------------------------------------------------
    private void rebuildGameArea() {
        double newWidth = currentCols * currentCellSize;
        double newHeight = currentRows * currentCellSize;

        Canvas newCanvas = new Canvas(newWidth, newHeight);
        GameBoard newBoard = new GameBoard(newCanvas, currentCellSize, currentCols, currentRows);
        newBoard.drawGrid(gridColor);

        HBox root = (HBox) primaryStage.getScene().getRoot();
        int canvasIndex = root.getChildren().indexOf(canvas);
        root.getChildren().set(canvasIndex, newCanvas);

        canvas = newCanvas;
        board = newBoard;
        alive.clear();
        generation = 0;
        generationLabel.setText("Generation: 0");
        lastUpdate = 0;
        canvas.setOnMouseClicked(event -> userInput(event.getX(), event.getY()));

        // Изменяем размер окна
        primaryStage.getScene().getWindow().setWidth(newWidth + UI_PANEL_WIDTH);
        primaryStage.getScene().getWindow().setHeight(newHeight + 40);
    }

    // -------------------------------------------------------------
    // Сохранение/загрузка симуляции
    // -------------------------------------------------------------
    private void saveSimulation() {
        if (alive.isEmpty()) {
            showAlert("Info", "No living cells to save.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save simulation");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Life Game files", "*.lif"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file == null) return;
        boolean ok = SimulationManager.saveToFile(file, alive, generation, currentCols, currentRows);
        showAlert(ok ? "Success" : "Error", ok ? "Simulation saved." : "Cannot save simulation.");
    }

    private void loadSimulation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load simulation");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Life Game files", "*.lif"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file == null) return;

        SimulationManager.FullSimulationData data = SimulationManager.loadFromFileFull(file);
        if (data == null) {
            showAlert("Error", "Cannot load simulation.\nFile may be corrupted.");
            return;
        }

        // Если размеры не совпадают, принудительно меняем настройки поля
        if (data.cols != currentCols || data.rows != currentRows) {
            colsSlider.setValue(data.cols);
            rowsSlider.setValue(data.rows);
            applyChanges(); // перестраивает поле с новыми размерами, очищает alive
        }

        // Останавливаем таймер и загружаем клетки
        if (timer != null) timer.stop();
        alive.clear();
        board.drawGrid(gridColor); // очищаем (на случай, если размеры не менялись)
        alive.addAll(data.alive);
        for (Cell cell : data.alive) {
            board.fillCell(cell.getX(), cell.getY(), cell.getColor());
        }
        generation = data.generation;
        generationLabel.setText("Generation: " + generation);
        lastUpdate = 0;
        showAlert("Success", "Simulation loaded. Field size: " + data.cols + "x" + data.rows + ", Generation " + generation);
    }

    // -------------------------------------------------------------
    // Игровая логика
    // -------------------------------------------------------------
    public void nextGeneration() {
        if (alive == null) return;
        Map<Cell, Integer> neighboursCount = new HashMap<>();
        for (Cell cell : alive) {
            neighboursCount.putIfAbsent(cell, 0);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = Math.floorMod(cell.getX() + dx, currentCols);
                    int ny = Math.floorMod(cell.getY() + dy, currentRows);
                    Cell neighbour = new Cell(nx, ny, cell.getColor());
                    neighboursCount.put(neighbour,
                            neighboursCount.getOrDefault(neighbour, 0) + 1);
                }
            }
        }
        HashSet<Cell> newAlive = new HashSet<>();
        for (Map.Entry<Cell, Integer> entry : neighboursCount.entrySet()) {
            Cell cell = entry.getKey();
            int neighbours = entry.getValue();
            boolean wasAlive = alive.contains(cell);
            if (wasAlive && (neighbours == 2 || neighbours == 3)) {
                newAlive.add(cell);
            } else if (!wasAlive && neighbours == 3) {
                Color mixed = getMixedColorFromNeighbors(cell);
                Cell newCell = new Cell(cell.getX(), cell.getY(), mixed);
                newAlive.add(newCell);
                board.fillCell(cell.getX(), cell.getY(), mixed);
            } else if (wasAlive) {
                board.clearCell(cell.getX(), cell.getY());
            }
        }
        alive = newAlive;
        generation++;
        generationLabel.setText("Generation: " + generation);
    }

    private void userInput(double x, double y) {
        int col = (int) (x / currentCellSize);
        int row = (int) (y / currentCellSize);
        if (col < 0 || col >= currentCols || row < 0 || row >= currentRows) return;
        Cell dummy = new Cell(col, row);
        if (alive.contains(dummy)) {
            alive.remove(dummy);
            board.clearCell(col, row);
        } else {
            Color color = getFirstNeighborColor(col, row);
            if (color == null) color = Constants.getRandomColor();
            alive.add(new Cell(col, row, color));
            board.fillCell(col, row, color);
        }
    }

    private void resetGame() {
        if (timer != null) timer.stop();
        generation = 0;
        generationLabel.setText("Generation: 0");
        board.drawGrid(gridColor);
        alive.clear();
        lastUpdate = 0;
    }

    private void randomGeneration() {
        if (timer != null) timer.stop();
        alive.clear();
        board.drawGrid(gridColor);
        int totalCells = currentCols * currentRows;
        int targetCount = (int) (totalCells * generationDensity / 100.0);
        if (targetCount == 0 && generationDensity > 0) targetCount = 1;
        HashSet<Cell> randomCells = new HashSet<>();
        while (randomCells.size() < targetCount) {
            int x = (int) (Math.random() * currentCols);
            int y = (int) (Math.random() * currentRows);
            randomCells.add(new Cell(x, y, Constants.getRandomColor()));
        }
        for (Cell cell : randomCells) {
            alive.add(cell);
            board.fillCell(cell.getX(), cell.getY(), cell.getColor());
        }
        generation = 0;
        generationLabel.setText("Generation: 0");
        lastUpdate = 0;
    }

    private Color getFirstNeighborColor(int col, int row) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = Math.floorMod(col + dx, currentCols);
                int ny = Math.floorMod(row + dy, currentRows);
                for (Cell cell : alive) {
                    if (cell.getX() == nx && cell.getY() == ny)
                        return cell.getColor();
                }
            }
        }
        return null;
    }

    private Color getMixedColorFromNeighbors(Cell cell) {
        List<Color> neighborColors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = Math.floorMod(cell.getX() + dx, currentCols);
                int ny = Math.floorMod(cell.getY() + dy, currentRows);
                for (Cell c : alive) {
                    if (c.getX() == nx && c.getY() == ny) {
                        neighborColors.add(c.getColor());
                        break;
                    }
                }
            }
        }
        if (neighborColors.isEmpty()) return Constants.getRandomColor();
        double sumR = 0, sumG = 0, sumB = 0;
        for (Color col : neighborColors) {
            sumR += col.getRed();
            sumG += col.getGreen();
            sumB += col.getBlue();
        }
        int size = neighborColors.size();
        int r = (int) Math.round(sumR / size * 255);
        int g = (int) Math.round(sumG / size * 255);
        int b = (int) Math.round(sumB / size * 255);
        return Color.rgb(r, g, b);
    }

    private void initTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (frameInterval != Long.MAX_VALUE && now - lastUpdate >= frameInterval) {
                    nextGeneration();
                    lastUpdate = now;
                }
            }
        };
    }

    private int computeCellSize(int cols, int rows) {
        int w = MAX_BOARD_WIDTH / cols;
        int h = MAX_BOARD_HEIGHT / rows;
        return Math.min(w, h);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void changeGridColor() {
        if (gridColor == Color.WHITE) {
            gridColor = Color.BLACK;
        }
        else {
            gridColor = Color.WHITE;
        }
        board.drawGrid(gridColor);
    }
    public static void setColor(Color color) {
        gridColor = color;
        gridToggler.setSelected(gridColor.equals(Color.BLACK));
    }
    public static String getGridColorString() {
        if (gridColor.equals(Color.BLACK)) {
            return "BLACK";
        }
        return "WHITE";
    }
}