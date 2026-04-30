import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {

    // ============================================================
    // CONSTANTS
    // ============================================================
    public static final int CELL_SIZE = 30;
    public static final int WIDTH     = Board.COLS * CELL_SIZE;
    public static final int HEIGHT    = Board.ROWS * CELL_SIZE;

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private Board          board;
    private GameController controller;
    private GameState      state;

    // ============================================================
    // RENDER TIMER — tách khỏi drop timer, đảm bảo UI mượt
    // ============================================================
    private Timer renderTimer;

    public GamePanel(Board board, GameController controller, GameState state) {
        this.board      = board;
        this.controller = controller;
        this.state      = state;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Input
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                controller.keyPressed(e.getKeyCode());
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                controller.keyReleased(e.getKeyCode());
            }
        });

        // Render loop ~60fps
        renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();
    }

    private int getCellSize() {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        return Math.max(1, Math.min(width / Board.COLS, height / Board.ROWS));
    }

    private int getBoardWidth() {
        return getCellSize() * Board.COLS;
    }

    private int getBoardHeight() {
        return getCellSize() * Board.ROWS;
    }

    private int getBoardOriginX() {
        return Math.max(0, (getWidth() - getBoardWidth()) / 2);
    }

    private int getBoardOriginY() {
        return Math.max(0, (getHeight() - getBoardHeight()) / 2);
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

        drawGrid(g2);
        drawBoard(g2);
        drawGhost(g2);
        drawCurrent(g2);
        drawOverlay(g2);
    }

    // ============================================================
    // DRAW METHODS
    // ============================================================
    private void drawGrid(Graphics2D g2) {
        int cellSize = getCellSize();
        int originX = getBoardOriginX();
        int originY = getBoardOriginY();

        g2.setColor(new Color(40, 40, 40));
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                g2.drawRect(originX + col * cellSize, originY + row * cellSize,
                            cellSize, cellSize);
            }
        }
    }

    private void drawBoard(Graphics2D g2) {
        int cellSize = getCellSize();
        int originX = getBoardOriginX();
        int originY = getBoardOriginY();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                Color c = board.getCell(row, col);
                if (c != null) drawCell(g2, col, row, c, 1.0f, originX, originY, cellSize);
            }
        }
    }

    private void drawGhost(Graphics2D g2) {
        Tetromino ghost = board.getGhost(controller.getCurrent());
        drawTetromino(g2, ghost, 0.25f);
    }

    private void drawCurrent(Graphics2D g2) {
        drawTetromino(g2, controller.getCurrent(), 1.0f);
    }

    private void drawTetromino(Graphics2D g2, Tetromino t, float alpha) {
        int cellSize = getCellSize();
        int originX = getBoardOriginX();
        int originY = getBoardOriginY();
        int[][] shape = t.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;
                int boardX = t.getX() + col;
                int boardY = t.getY() + row;
                if (boardY < 0) continue;
                drawCell(g2, boardX, boardY, t.getColor(), alpha, originX, originY, cellSize);
            }
        }
    }

    private void drawCell(Graphics2D g2, int col, int row, Color color, float alpha,
                          int originX, int originY, int cellSize) {
        AlphaComposite ac = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);

        // Fill
        g2.setColor(color);
        g2.fillRect(originX + col * cellSize + 1, originY + row * cellSize + 1,
                    cellSize - 2, cellSize - 2);

        // Highlight top-left edge
        g2.setColor(color.brighter());
        g2.drawLine(originX + col * cellSize + 1, originY + row * cellSize + 1,
                    originX + col * cellSize + cellSize - 2, originY + row * cellSize + 1);
        g2.drawLine(originX + col * cellSize + 1, originY + row * cellSize + 1,
                    originX + col * cellSize + 1, originY + row * cellSize + cellSize - 2);

        // Reset composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void drawOverlay(Graphics2D g2) {
        GameState.State s = state.getCurrentState();
        int boardCenterX = getBoardOriginX() + getBoardWidth() / 2;
        int boardCenterY = getBoardOriginY() + getBoardHeight() / 2;
        if (s == GameState.State.PAUSED) {
            drawCenteredText(g2, "PAUSED", boardCenterX, boardCenterY, 36);
        } else if (s == GameState.State.GAME_OVER) {
            drawCenteredText(g2, "GAME OVER", boardCenterX, boardCenterY - 20, 36);
            drawCenteredText(g2, "Press R to restart", boardCenterX, boardCenterY + 30, 18);
        }
    }

    private void drawCenteredText(Graphics2D g2, String text, int cx, int cy, int fontSize) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        int y = cy + fm.getAscent() / 2;
        g2.drawString(text, x, y);
    }
}