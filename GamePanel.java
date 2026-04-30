import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    final int FPS = 60;
    Thread gameThread;
    GameController gameController;
    
    public GamePanel(){
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        gameController = new GameController();
    }

    public void lauchGame (){
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        double DrawInterval = 1000000000/FPS;
        long lastTime = System.nanoTime();
        double delta = 0;
        long currentTime;
         
        while (gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / DrawInterval;  
            lastTime = currentTime;
            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        gameController.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        gameController.paint(g2);
    }


}
