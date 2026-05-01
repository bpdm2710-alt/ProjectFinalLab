import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    private static void applyWindowBounds(JFrame frame, Rectangle bounds, Dimension minimumSize) {
    Rectangle screenBounds = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .getMaximumWindowBounds();

    int width = Math.max(bounds.width, minimumSize.width);
    int height = Math.max(bounds.height, minimumSize.height);
    width = Math.min(width, screenBounds.width);
    height = Math.min(height, screenBounds.height);

    int maxX = screenBounds.x + screenBounds.width - width;
    int maxY = screenBounds.y + screenBounds.height - height;
    int x = Math.max(screenBounds.x, Math.min(bounds.x, maxX));
    int y = Math.max(screenBounds.y, Math.min(bounds.y, maxY));

        frame.setSize(width, height);
    frame.setLocation(x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Board            board   = new Board();
            TetrominoFactory factory = new TetrominoFactory();
            GameState        state   = new GameState();
            GameController   ctrl    = new GameController(board, factory, state);

            GamePanel    gamePanel    = new GamePanel(board, ctrl, state);
            SidebarPanel sidebarPanel = new SidebarPanel(state, ctrl, factory);

            JFrame frame = new JFrame("Tetris");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.setLayout(new BorderLayout());
            frame.add(gamePanel,    BorderLayout.CENTER);
            frame.add(sidebarPanel, BorderLayout.EAST);
            frame.pack();
            Dimension minimumSize = new Dimension(
                    GamePanel.WIDTH + SidebarPanel.WIDTH,
                    Math.max(GamePanel.HEIGHT, SidebarPanel.HEIGHT)
            );
            frame.setMinimumSize(minimumSize);
            gamePanel.setMinimumSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT));
            sidebarPanel.setMinimumSize(new Dimension(SidebarPanel.WIDTH, SidebarPanel.HEIGHT));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            SoundManager.getInstance().startBackgroundMusic();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    SoundManager.getInstance().stopBackgroundMusic();
                }
            });

            final Rectangle[] restoreBounds = { frame.getBounds() };
            final boolean[] fullscreen = { false };
            final GraphicsDevice screenDevice = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();

            Runnable enterFullscreen = () -> {
                if (fullscreen[0]) return;
                fullscreen[0] = true;
                restoreBounds[0] = frame.getBounds();
                frame.dispose();
                frame.setUndecorated(true);
                screenDevice.setFullScreenWindow(frame);
                frame.revalidate();
                frame.repaint();
                gamePanel.requestFocusInWindow();
            };

            Runnable exitFullscreen = () -> {
                if (!fullscreen[0]) return;
                fullscreen[0] = false;
                screenDevice.setFullScreenWindow(null);
                frame.dispose();
                frame.setUndecorated(false);
                frame.setExtendedState(JFrame.NORMAL);
                applyWindowBounds(frame, restoreBounds[0], minimumSize);
                frame.setVisible(true);
                frame.revalidate();
                frame.repaint();
                gamePanel.requestFocusInWindow();
            };

            Action toggleFullscreen = new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (fullscreen[0]) exitFullscreen.run();
                    else enterFullscreen.run();
                }
            };

            Action exitFullscreenAction = new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    exitFullscreen.run();
                }
            };

            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0), "toggleFullscreen");
            frame.getRootPane().getActionMap().put("toggleFullscreen", toggleFullscreen);
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "exitFullscreen");
            frame.getRootPane().getActionMap().put("exitFullscreen", exitFullscreenAction);

            gamePanel.requestFocusInWindow();
        });
    }
}