import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 900;
    final int FPS = 60;
    Thread gameThread;
    
    public GamePanel(){
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setLayout(null);
    }

    public void lauchGame (){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (gameThread != null){
            
        }
    }

    public void update() {

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }


}
