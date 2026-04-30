import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SidebarPanel extends JPanel {

    // ============================================================
    // CONSTANTS
    // ============================================================
    public static final int WIDTH     = 200;
    public static final int HEIGHT    = GamePanel.HEIGHT;
    private static final int CELL_SIZE = 25;
    private static final int PADDING   = 20;

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private GameState        state;
    private GameController   controller;
    private TetrominoFactory factory;

    public SidebarPanel(GameState state, GameController controller,
                        TetrominoFactory factory) {
        this.state      = state;
        this.controller = controller;
        this.factory    = factory;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(20, 20, 20));

        // Observer — tự repaint khi score/level thay đổi
        state.addListener(this::repaint);
    }

    // ============================================================
    // RENDER
    // ============================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int y = PADDING;
        y = drawStats(g2, y);
        y = drawHold(g2, y + 20);
            drawPreview(g2, y + 20);
    }

    // ============================================================
    // STATS — score, level, lines
    // ============================================================
    private int drawStats(Graphics2D g2, int startY) {
        int y = startY;
        y = drawLabel(g2, "SCORE",  String.valueOf(state.getScore()),  y);
        y = drawLabel(g2, "LEVEL",  String.valueOf(state.getLevel()),  y + 10);
        y = drawLabel(g2, "LINES",  String.valueOf(state.getLinesCleared()), y + 10);
        return y;
    }

    private int drawLabel(Graphics2D g2, String title, String value, int y) {
        // Title
        g2.setColor(new Color(160, 160, 160));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString(title, PADDING, y);

        // Value
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString(value, PADDING, y + 26);

        return y + 48;
    }

    // ============================================================
    // HOLD PIECE
    // ============================================================
    private int drawHold(Graphics2D g2, int startY) {
        drawSectionTitle(g2, "HOLD", startY);

        int boxY = startY + 20;
        drawTetrominoBox(g2, controller.getHeld(), boxY);

        return boxY + CELL_SIZE * 4;
    }

    // ============================================================
    // PREVIEW — 3 khối tiếp theo
    // ============================================================
    private void drawPreview(Graphics2D g2, int startY) {
        drawSectionTitle(g2, "NEXT", startY);

        List<Tetromino> previews = factory.preview(3);
        int y = startY + 20;
        for (Tetromino t : previews) {
            drawTetrominoBox(g2, t, y);
            y += CELL_SIZE * 3 + 10;
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private void drawSectionTitle(Graphics2D g2, String title, int y) {
        g2.setColor(new Color(160, 160, 160));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString(title, PADDING, y);

        // Underline
        g2.setColor(new Color(80, 80, 80));
        g2.drawLine(PADDING, y + 4, WIDTH - PADDING, y + 4);
    }

    private void drawTetrominoBox(Graphics2D g2, Tetromino t, int boxY) {
        // Background box
        g2.setColor(new Color(35, 35, 35));
        g2.fillRoundRect(PADDING - 4, boxY,
                         WIDTH - PADDING * 2 + 8, CELL_SIZE * 3,
                         8, 8);

        if (t == null) return;

        int[][] shape = t.getShape();

        // Tính offset để center khối trong box
        int blockCols = 0, blockRows = 0;
        for (int row = 0; row < shape.length; row++)
            for (int col = 0; col < shape[row].length; col++)
                if (shape[row][col] == 1) {
                    blockCols = Math.max(blockCols, col + 1);
                    blockRows = Math.max(blockRows, row + 1);
                }

        int boxWidth  = WIDTH - PADDING * 2;
        int offsetX   = PADDING + (boxWidth - blockCols * CELL_SIZE) / 2;
        int offsetY   = boxY   + (CELL_SIZE * 3 - blockRows * CELL_SIZE) / 2;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;
                int px = offsetX + col * CELL_SIZE;
                int py = offsetY + row * CELL_SIZE;

                // Fill
                g2.setColor(t.getColor());
                g2.fillRect(px + 1, py + 1, CELL_SIZE - 2, CELL_SIZE - 2);

                // Highlight
                g2.setColor(t.getColor().brighter());
                g2.drawLine(px + 1, py + 1, px + CELL_SIZE - 2, py + 1);
                g2.drawLine(px + 1, py + 1, px + 1, py + CELL_SIZE - 2);
            }
        }
    }
}