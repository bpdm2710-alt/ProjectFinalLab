import javax.swing.JPanel;
import java.awt.*;
public class GamePanel extends JPanel {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 900;
    
    public GamePanel(){
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setLayout(null);
    }
}
