package wolf;

import javafx.scene.paint.Color;

public class Constants {
    private static final int HEIGHT_CELLS = 30;
    private static final int WIDTH_CELLS = 30;
    private static final int FPS = 10;
    private static final Color[] COLOR_SET = {
            Color.BLUE, Color.RED, Color.YELLOW, Color.PURPLE, Color.GREEN,
            Color.CYAN, Color.MAGENTA, Color.PINK, Color.ORANGE, Color.BROWN,
            Color.LIME, Color.TEAL, Color.CORAL, Color.GOLD, Color.TURQUOISE,
            Color.VIOLET, Color.HOTPINK, Color.DEEPPINK, Color.LAVENDER, Color.PLUM
    };

    public static int getHeightCells() {
        return HEIGHT_CELLS;
    }
    public static int getWidthCells() {
        return WIDTH_CELLS;
    }
    public static int getFps() {
        return FPS;
    }
    public static Color getRandomColor() {
        int rand = (int)(COLOR_SET.length * Math.random());
        return COLOR_SET[rand];
    }
}
