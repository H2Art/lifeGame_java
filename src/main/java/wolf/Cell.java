package wolf;

import javafx.scene.paint.Color;

import java.util.Objects;

public class Cell {
    private final int x;
    private final int y;
    private final Color color;
    public Cell() {
        x = 0;
        y = 0;
        color = Constants.getRandomColor();
    }
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        color = Constants.getRandomColor();
    }
    public Cell(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x == cell.x && y == cell.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
