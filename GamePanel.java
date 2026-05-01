import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GamePanel extends JPanel {

    private static class UiButton {
        private final String id;
        private final String label;
        private final Rectangle bounds;
        private final int keyCode;
        private final boolean emphasize;

        private UiButton(String id, String label, Rectangle bounds, int keyCode, boolean emphasize) {
            this.id = id;
            this.label = label;
            this.bounds = bounds;
            this.keyCode = keyCode;
            this.emphasize = emphasize;
        }
    }

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
    private long audioHudVisibleUntil;
    private String hoveredButtonId;
    private String pressedButtonId;

    public GamePanel(Board board, GameController controller, GameState state) {
        this.board      = board;
        this.controller = controller;
        this.state      = state;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        ThemeManager.ThemePalette palette = ThemeManager.getPalette();
        setBackground(palette.boardBackground);
        setFocusable(true);
        audioHudVisibleUntil = 0;

        // Input
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                controller.keyPressed(e.getKeyCode());
                if (isAudioControlKey(e.getKeyCode())) {
                    showAudioHudFor(2500);
                }
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                controller.keyReleased(e.getKeyCode());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updatePressedButton(e.getPoint());
                handleMousePress(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updatePressedButton(null);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateHoveredButton(null);
                updatePressedButton(null);
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredButton(e.getPoint());
            }
        });

        // Render loop ~60fps
        renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();
    }

    private boolean isAudioControlKey(int keyCode) {
        return keyCode == KeyEvent.VK_M
                || keyCode == KeyEvent.VK_N
                || keyCode == KeyEvent.VK_F8
                || keyCode == KeyEvent.VK_F7
                || keyCode == KeyEvent.VK_F6
                || keyCode == KeyEvent.VK_F5;
    }

    private void showAudioHudFor(int milliseconds) {
        audioHudVisibleUntil = System.currentTimeMillis() + milliseconds;
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
        ThemeManager.ThemePalette palette = ThemeManager.getPalette();
        setBackground(palette.boardBackground);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        if (state.getCurrentState() == GameState.State.MENU) {
            drawMenuOverlay(g2, palette);
        } else if (state.getCurrentState() == GameState.State.SETTINGS) {
            drawSettingsOverlay(g2, palette);
        } else {
            drawGrid(g2);
            drawBoard(g2);
            drawGhost(g2);
            drawCurrent(g2);
        }
        if (state.getCurrentState() != GameState.State.MENU
                && state.getCurrentState() != GameState.State.SETTINGS) {
            drawAudioHud(g2, palette);
        }
        drawOverlay(g2);
    }

    // ============================================================
    // DRAW METHODS
    // ============================================================
    private void drawGrid(Graphics2D g2) {
        int cellSize = getCellSize();
        int originX = getBoardOriginX();
        int originY = getBoardOriginY();

        ThemeManager.ThemePalette palette = ThemeManager.getPalette();
        g2.setColor(palette.grid);
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
        ThemeManager.ThemePalette palette = ThemeManager.getPalette();
        if (s == GameState.State.PAUSED) {
            drawCenteredText(g2, "PAUSED", boardCenterX, boardCenterY, 36, palette.text);
        } else if (s == GameState.State.GAME_OVER) {
            drawCenteredText(g2, "GAME OVER", boardCenterX, boardCenterY - 20, 36, palette.text);
            drawCenteredText(g2, "Press R to restart", boardCenterX, boardCenterY + 30, 18, palette.textSecondary);
        }
    }

    private void drawMenuOverlay(Graphics2D g2, ThemeManager.ThemePalette palette) {
        Rectangle card = getOverlayCardBounds(0.90, 0.76, 280, 220);
        int boardCenterX = card.x + card.width / 2;
        SoundManager sm = SoundManager.getInstance();
        String themeText = "THEME: " + formatThemeName(ThemeManager.getCurrentTheme());
        String bestText = "BEST: " + state.getHighScore();
        String bgmText = sm.isBackgroundMusicMuted() ? "BGM: OFF" : "BGM: ON";
        String sfxText = sm.isSfxMuted() ? "SFX: OFF" : "SFX: ON";
        int titleSize = clampInt(card.height / 6, 24, 42);
        int mainSize = clampInt(card.height / 14, 13, 20);
        int subSize = clampInt(card.height / 18, 11, 15);
        int padTop = clampInt(card.height / 14, 12, 24);
        int y = card.y + padTop;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(card.x, card.y, card.width, card.height, 18, 18);
        g2.setColor(palette.grid);
        g2.drawRoundRect(card.x, card.y, card.width, card.height, 18, 18);

        drawCenteredText(g2, "TETRIS", boardCenterX, y + titleSize / 2, titleSize, palette.text);
        y += titleSize + clampInt(card.height / 20, 6, 14);
        drawCenteredText(g2, "Press Enter to Start", boardCenterX, y + mainSize / 2, mainSize, palette.textSecondary);
        y += mainSize + clampInt(card.height / 26, 5, 10);
        drawCenteredText(g2, themeText, boardCenterX, y + subSize / 2, subSize, palette.text);
        y += subSize + 4;
        drawCenteredText(g2, bestText + "    " + bgmText + "    " + sfxText, boardCenterX, y + subSize / 2, subSize, palette.textSecondary);
        y += subSize + 4;
        drawCenteredText(g2, "S: Settings   T: Theme   M/N: Audio", boardCenterX, y + subSize / 2, subSize, palette.textSecondary);

        for (UiButton button : getMenuButtons(card)) {
            drawButton(g2, button, palette,
                    button.id.equals(hoveredButtonId),
                    button.id.equals(pressedButtonId));
        }
    }

    private String formatThemeName(ThemeManager.Theme theme) {
        return theme.name().replace('_', ' ');
    }

    private void drawSettingsOverlay(Graphics2D g2, ThemeManager.ThemePalette palette) {
        Rectangle card = getOverlayCardBounds(0.92, 0.92, 300, 270);
        int boardCenterX = card.x + card.width / 2;
        SoundManager sm = SoundManager.getInstance();
        int titleSize = clampInt(card.height / 7, 22, 36);
        int textSize = clampInt(card.height / 17, 12, 18);
        int hintSize = clampInt(card.height / 23, 10, 13);
        int y = card.y + clampInt(card.height / 12, 12, 22);

        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRoundRect(card.x, card.y, card.width, card.height, 18, 18);
        g2.setColor(palette.grid);
        g2.drawRoundRect(card.x, card.y, card.width, card.height, 18, 18);

        drawCenteredText(g2, "SETTINGS", boardCenterX, y + titleSize / 2, titleSize, palette.text);
        y += titleSize + clampInt(card.height / 26, 6, 12);
        drawCenteredText(g2, "Theme: " + formatThemeName(ThemeManager.getCurrentTheme()), boardCenterX, y + textSize / 2, textSize, palette.text);
        y += textSize + 4;
        drawCenteredText(g2, "BGM: " + (sm.isBackgroundMusicMuted() ? "OFF" : "ON") + "   SFX: " + (sm.isSfxMuted() ? "OFF" : "ON"), boardCenterX, y + hintSize / 2, hintSize, palette.textSecondary);
        y += hintSize + 4;
        drawCenteredText(g2, "SFX detected: " + sm.getAvailableSfxCount() + "/7", boardCenterX, y + hintSize / 2, hintSize, palette.textSecondary);

        drawSfxStatusPanel(g2, card, sm, palette);

        for (UiButton button : getSettingsButtons(card, sm)) {
            drawButton(g2, button, palette,
                    button.id.equals(hoveredButtonId),
                    button.id.equals(pressedButtonId));
        }
    }

    private void drawSfxStatusPanel(Graphics2D g2, Rectangle card, SoundManager sm, ThemeManager.ThemePalette palette) {
        Map<String, String> status = sm.getSfxStatusMap();
        int panelMarginX = clampInt(card.width / 16, 14, 20);
        int panelY = card.y + clampInt(card.height / 4, 62, 94);
        int panelWidth = card.width - panelMarginX * 2;
        int panelHeight = clampInt(card.height / 4, 68, 92);
        int titleSize = clampInt(card.height / 26, 10, 13);
        int rowSize = clampInt(card.height / 28, 9, 12);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(card.x + panelMarginX, panelY, panelWidth, panelHeight, 10, 10);
        g2.setColor(palette.grid);
        g2.drawRoundRect(card.x + panelMarginX, panelY, panelWidth, panelHeight, 10, 10);

        int centerX = card.x + card.width / 2;
        drawCenteredText(g2, "SFX STATUS", centerX, panelY + titleSize + 2, titleSize, palette.textSecondary);

        int leftX = card.x + panelMarginX + 10;
        int rightX = card.x + panelMarginX + panelWidth / 2 + 4;
        int rowY = panelY + titleSize + 14;
        int lineHeight = Math.max(11, rowSize + 2);
        int col = 0;
        int row = 0;

        g2.setFont(new Font("Monospaced", Font.PLAIN, rowSize));
        for (Map.Entry<String, String> entry : status.entrySet()) {
            int x = (col == 0) ? leftX : rightX;
            int y = rowY + row * lineHeight;
            g2.setColor(palette.text);
            g2.drawString(entry.getKey(), x, y);
            g2.setColor(palette.textSecondary);
            g2.drawString(" " + simplifySfxStatus(entry.getValue()), x + clampInt(panelWidth / 6, 40, 78), y);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
    }

    private String simplifySfxStatus(String status) {
        if (status == null) return "SYNTH";
        if (status.startsWith("FILE: ")) {
            String name = status.substring(6);
            if (name.length() > 10) {
                return "F:" + name.substring(0, 7) + "...";
            }
            return "F:" + name;
        }
        return status;
    }

    private void drawAudioHud(Graphics2D g2, ThemeManager.ThemePalette palette) {
        if (System.currentTimeMillis() > audioHudVisibleUntil) return;

        SoundManager sm = SoundManager.getInstance();
        String bgmText = sm.isBackgroundMusicMuted()
                ? "BGM: OFF"
                : "BGM: " + sm.getBackgroundMusicVolumePercent() + "%";
        String sfxText = sm.isSfxMuted()
                ? "SFX: OFF"
                : "SFX: " + sm.getSfxVolumePercent() + "%";
        String bgmSourceText = "SRC: " + sm.getBackgroundMusicStatus();

        int originX = getBoardOriginX();
        int originY = getBoardOriginY();

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();

        int textWidth = Math.max(fm.stringWidth(bgmText), fm.stringWidth(sfxText));
        textWidth = Math.max(textWidth, fm.stringWidth(bgmSourceText));
        int boxWidth = textWidth + 18;
        int boxHeight = fm.getHeight() * 3 + 16;
        int boxX = originX + 8;
        int boxY = originY + 8;

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        g2.setColor(palette.grid);
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

        g2.setColor(palette.text);
        g2.drawString(bgmText, boxX + 9, boxY + fm.getAscent() + 4);
        g2.drawString(sfxText, boxX + 9, boxY + fm.getAscent() + fm.getHeight() + 4);
        g2.drawString(bgmSourceText, boxX + 9, boxY + fm.getAscent() + fm.getHeight() * 2 + 4);
    }

    private void drawCenteredText(Graphics2D g2, String text, int cx, int cy, int fontSize, Color color) {
        g2.setColor(color);
        g2.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        int y = cy + fm.getAscent() / 2;
        g2.drawString(text, x, y);
    }

    private void drawButton(Graphics2D g2, UiButton button, ThemeManager.ThemePalette palette, boolean hovered, boolean pressed) {
        Rectangle bounds = button.bounds;
        Color fillColor;
        if (pressed) {
            fillColor = new Color(25, 25, 25, 240);
        } else if (hovered) {
            fillColor = new Color(60, 60, 60, 230);
        } else {
            fillColor = button.emphasize ? new Color(0, 0, 0, 170) : new Color(35, 35, 35, 210);
        }

        g2.setColor(fillColor);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        g2.setColor(pressed ? palette.textSecondary : hovered ? palette.text : palette.grid);
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        g2.setColor(pressed ? palette.textSecondary : palette.text);
        g2.setFont(new Font("Monospaced", Font.BOLD, clampInt(bounds.height - 12, 11, 15)));
        FontMetrics fm = g2.getFontMetrics();
        int x = bounds.x + (bounds.width - fm.stringWidth(button.label)) / 2 + (pressed ? 1 : 0);
        int y = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2 + (pressed ? 1 : 0);
        g2.drawString(button.label, x, y);
    }

    private void handleMousePress(Point point) {
        UiButton button = findButtonAt(point);
        if (button != null) {
            controller.keyPressed(button.keyCode);
        }
    }

    private void updateHoveredButton(Point point) {
        UiButton button = findButtonAt(point);
        String nextHovered = (button == null) ? null : button.id;

        if (nextHovered == null ? hoveredButtonId != null : !nextHovered.equals(hoveredButtonId)) {
            hoveredButtonId = nextHovered;
            repaint();
        }
    }

    private void updatePressedButton(Point point) {
        UiButton button = findButtonAt(point);
        String nextPressed = (button == null) ? null : button.id;

        if (nextPressed == null ? pressedButtonId != null : !nextPressed.equals(pressedButtonId)) {
            pressedButtonId = nextPressed;
            repaint();
        }
    }

    private UiButton findButtonAt(Point point) {
        if (point == null) return null;
        for (UiButton button : getActiveButtons()) {
            if (button.bounds.contains(point)) return button;
        }
        return null;
    }

    private List<UiButton> getActiveButtons() {
        if (state.getCurrentState() == GameState.State.MENU) {
            Rectangle card = getOverlayCardBounds(0.90, 0.76, 280, 220);
            return getMenuButtons(card);
        }
        if (state.getCurrentState() == GameState.State.SETTINGS) {
            Rectangle card = getOverlayCardBounds(0.92, 0.92, 300, 270);
            return getSettingsButtons(card, SoundManager.getInstance());
        }
        return new ArrayList<>();
    }

    private List<UiButton> getMenuButtons(Rectangle card) {
        List<UiButton> buttons = new ArrayList<>();
        int buttonWidth = Math.min(220, card.width - 32);
        int buttonHeight = clampInt(card.height / 9, 26, 36);
        int gap = clampInt(card.height / 24, 6, 10);
        int x = card.x + (card.width - buttonWidth) / 2;
        int yStart = card.y + card.height - buttonHeight * 2 - gap - 14;

        buttons.add(new UiButton("menu.start", "START", new Rectangle(x, yStart, buttonWidth, buttonHeight), KeyEvent.VK_ENTER, true));
        buttons.add(new UiButton("menu.settings", "SETTINGS", new Rectangle(x, yStart + buttonHeight + gap, buttonWidth, buttonHeight), KeyEvent.VK_S, false));
        return buttons;
    }

    private List<UiButton> getSettingsButtons(Rectangle card, SoundManager sm) {
        List<UiButton> buttons = new ArrayList<>();
        int gap = clampInt(card.height / 28, 6, 10);
        int buttonHeight = clampInt(card.height / 10, 22, 34);
        int innerMargin = 16;
        int fullWidth = card.width - innerMargin * 2;
        int halfWidth = (fullWidth - gap) / 2;
        int x1 = card.x + innerMargin;
        int x2 = x1 + halfWidth + gap;

        int topLimit = card.y + clampInt((int) (card.height * 0.56), 120, 210);
        int backY = card.y + card.height - buttonHeight - 12;
        int neededHeight = buttonHeight * 5 + gap * 4;
        int availableHeight = Math.max(buttonHeight, (backY + buttonHeight) - topLimit);
        if (neededHeight > availableHeight) {
            int reducedHeight = Math.max(18, (availableHeight - gap * 4) / 5);
            buttonHeight = reducedHeight;
            backY = card.y + card.height - buttonHeight - 12;
        }

        int row4Y = backY - gap - buttonHeight;
        int row3Y = row4Y - gap - buttonHeight;
        int row2Y = row3Y - gap - buttonHeight;
        int themeY = row2Y - gap - buttonHeight;

        buttons.add(new UiButton("settings.theme", "CYCLE THEME", new Rectangle(x1, themeY, fullWidth, buttonHeight), KeyEvent.VK_T, true));
        buttons.add(new UiButton("settings.bgmToggle", sm.isBackgroundMusicMuted() ? "BGM ON" : "BGM OFF", new Rectangle(x1, row2Y, halfWidth, buttonHeight), KeyEvent.VK_M, false));
        buttons.add(new UiButton("settings.sfxToggle", sm.isSfxMuted() ? "SFX ON" : "SFX OFF", new Rectangle(x2, row2Y, halfWidth, buttonHeight), KeyEvent.VK_N, false));
        buttons.add(new UiButton("settings.bgmDown", "BGM -", new Rectangle(x1, row3Y, halfWidth, buttonHeight), KeyEvent.VK_F7, false));
        buttons.add(new UiButton("settings.bgmUp", "BGM +", new Rectangle(x2, row3Y, halfWidth, buttonHeight), KeyEvent.VK_F8, false));
        buttons.add(new UiButton("settings.sfxDown", "SFX -", new Rectangle(x1, row4Y, halfWidth, buttonHeight), KeyEvent.VK_F5, false));
        buttons.add(new UiButton("settings.sfxUp", "SFX +", new Rectangle(x2, row4Y, halfWidth, buttonHeight), KeyEvent.VK_F6, false));
        buttons.add(new UiButton("settings.back", "BACK", new Rectangle(x1, backY, fullWidth, buttonHeight), KeyEvent.VK_ESCAPE, true));

        return buttons;
    }

    private Rectangle getOverlayCardBounds(double widthRatio, double heightRatio, int minWidth, int minHeight) {
        int boardW = getBoardWidth();
        int boardH = getBoardHeight();
        int originX = getBoardOriginX();
        int originY = getBoardOriginY();

        int safeMaxW = Math.max(120, boardW - 8);
        int safeMaxH = Math.max(140, boardH - 8);
        int safeMinW = Math.min(minWidth, safeMaxW);
        int safeMinH = Math.min(minHeight, safeMaxH);
        int cardW = clampInt((int) (boardW * widthRatio), safeMinW, safeMaxW);
        int cardH = clampInt((int) (boardH * heightRatio), safeMinH, safeMaxH);

        int x = originX + (boardW - cardW) / 2;
        int y = originY + (boardH - cardH) / 2;
        return new Rectangle(x, y, cardW, cardH);
    }

    private int clampInt(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }
}