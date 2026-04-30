import javax.swing.*;
import java.awt.BorderLayout;

public class Main {

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
            frame.setResizable(false);
            frame.setLayout(new BorderLayout());
            frame.add(gamePanel,    BorderLayout.CENTER);
            frame.add(sidebarPanel, BorderLayout.EAST);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            gamePanel.requestFocusInWindow();
            ctrl.start();
        });
    }
}