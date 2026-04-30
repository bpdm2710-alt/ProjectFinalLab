import java.awt.*;
public class GameController {
    final int WIDTH = 300;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    public GameController() {
        left_x = (GamePanel.WIDTH - WIDTH) / 2;
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;
    }

    public void update() {

    }
    public void paint(Graphics2D g2) {
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x-8, top_y-8, WIDTH+16, HEIGHT+16);

        int x = right_x + 100;
        int y = top_y + 50;
        g2.drawRect(x, y, 200,500);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("PREVIEW", x+56, y+30);   
    }

}
