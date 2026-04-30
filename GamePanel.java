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
                controller.handleKey(e.getKeyCode());
                repaint();
            }
        });

        // Render loop ~60fps
        renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();
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
        g2.setColor(new Color(40, 40, 40));
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                g2.drawRect(col * CELL_SIZE, row * CELL_SIZE,
                            CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawBoard(Graphics2D g2) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                Color c = board.getCell(row, col);
                if (c != null) drawCell(g2, col, row, c, 1.0f);
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
        int[][] shape = t.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;
                int boardX = t.getX() + col;
                int boardY = t.getY() + row;
                if (boardY < 0) continue;
                drawCell(g2, boardX, boardY, t.getColor(), alpha);
            }
        }
    }

    private void drawCell(Graphics2D g2, int col, int row, Color color, float alpha) {
        AlphaComposite ac = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);

        // Fill
        g2.setColor(color);
        g2.fillRect(col * CELL_SIZE + 1, row * CELL_SIZE + 1,
                    CELL_SIZE - 2, CELL_SIZE - 2);

        // Highlight top-left edge
        g2.setColor(color.brighter());
        g2.drawLine(col * CELL_SIZE + 1, row * CELL_SIZE + 1,
                    col * CELL_SIZE + CELL_SIZE - 2, row * CELL_SIZE + 1);
        g2.drawLine(col * CELL_SIZE + 1, row * CELL_SIZE + 1,
                    col * CELL_SIZE + 1, row * CELL_SIZE + CELL_SIZE - 2);

        // Reset composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void drawOverlay(Graphics2D g2) {
        GameState.State s = state.getCurrentState();
        if (s == GameState.State.PAUSED) {
            drawCenteredText(g2, "PAUSED", WIDTH / 2, HEIGHT / 2, 36);
        } else if (s == GameState.State.GAME_OVER) {
            drawCenteredText(g2, "GAME OVER", WIDTH / 2, HEIGHT / 2 - 20, 36);
            drawCenteredText(g2, "Press R to restart", WIDTH / 2, HEIGHT / 2 + 30, 18);
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