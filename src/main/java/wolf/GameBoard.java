package wolf;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameBoard {
    private final GraphicsContext gc;
    private final int cellSize;
    private final int cols;
    private final int rows;

    public GameBoard(Canvas canvas, int cellSize, int cols, int rows) {
        this.gc = canvas.getGraphicsContext2D();
        this.cellSize = cellSize;
        this.cols = cols;
        this.rows = rows;
    }

    public void drawGrid() {
        double width = cols * cellSize;
        double height = rows * cellSize;
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        for (double x = 0; x <= width; x += cellSize) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double y = 0; y <= height; y += cellSize) {
            gc.strokeLine(0, y, width, y);
        }
    }

    public void fillCell(int col, int row, Color color) {
        double px = col * cellSize;
        double py = row * cellSize;
        gc.setFill(color);
        gc.fillRect(px + 1, py + 1, cellSize - 2, cellSize - 2);
    }

    public void clearCell(int col, int row) {
        fillCell(col, row, Color.BLACK);
    }
}