import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Tetris");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();

        window.setVisible(true);
        window.setResizable(false);
        window.setLocationRelativeTo(null);
    }
}   
