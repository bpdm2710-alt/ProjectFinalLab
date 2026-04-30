import java.awt.*;

public class Block extends Rectangle {
    public int x, y;
    public static final int SIZE = 30;
    public Color c;

    public Block(int x, int y, Color c) {
        this.c = c;
    }

public void draw(Graphics2D g2) {
        g2.setColor(c);
        g2.fillRect(x, y, SIZE, SIZE);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x, y, SIZE, SIZE);
    }
    
}
