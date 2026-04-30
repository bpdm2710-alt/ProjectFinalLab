import java.awt.Color;

public class Tetromino {

    // ============================================================
    // SHAPE DATA — int[shape][rotation][row][col]
    // 1 = filled, 0 = empty
    // ============================================================
    public static final int[][][][] SHAPES = {
        // I
        {
            {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}},
            {{0,0,1,0},{0,0,1,0},{0,0,1,0},{0,0,1,0}},
            {{0,0,0,0},{0,0,0,0},{1,1,1,1},{0,0,0,0}},
            {{0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0}}
        },
        // O
        {
            {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}
        },
        // T
        {
            {{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,0,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
            {{0,0,0,0},{1,1,1,0},{0,1,0,0},{0,0,0,0}},
            {{0,1,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}
        },
        // S
        {
            {{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,0,0},{0,1,1,0},{0,0,1,0},{0,0,0,0}},
            {{0,0,0,0},{0,1,1,0},{1,1,0,0},{0,0,0,0}},
            {{1,0,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}
        },
        // Z
        {
            {{1,1,0,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,0,1,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
            {{0,0,0,0},{1,1,0,0},{0,1,1,0},{0,0,0,0}},
            {{0,1,0,0},{1,1,0,0},{1,0,0,0},{0,0,0,0}}
        },
        // J
        {
            {{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,1,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}},
            {{0,0,0,0},{1,1,1,0},{0,0,1,0},{0,0,0,0}},
            {{0,1,0,0},{0,1,0,0},{1,1,0,0},{0,0,0,0}}
        },
        // L
        {
            {{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
            {{0,1,0,0},{0,1,0,0},{0,1,1,0},{0,0,0,0}},
            {{0,0,0,0},{1,1,1,0},{1,0,0,0},{0,0,0,0}},
            {{1,1,0,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}}
        }
    };

    public static final Color[] COLORS = {
        new Color(0,   240, 240), // I — cyan
        new Color(240, 240,   0), // O — yellow
        new Color(160,   0, 240), // T — purple
        new Color(0,   240,   0), // S — green
        new Color(240,   0,   0), // Z — red
        new Color(0,     0, 240), // J — blue
        new Color(240, 160,   0)  // L — orange
    };

    // ============================================================
    // INSTANCE FIELDS
    // ============================================================
    private int type;         // 0–6 tương ứng SHAPES
    private int rotation;     // 0–3
    private int x, y;         // vị trí top-left trên board (cột, hàng)

    public Tetromino(int type) {
        this.type     = type;
        this.rotation = 0;
        this.x        = 3;   // spawn giữa board 10 cột
        this.y        = 0;
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    public int[][] getShape() { return SHAPES[type][rotation]; }
    public Color getColor()   { return COLORS[type]; }
    public int getX()         { return x; }
    public int getY()         { return y; }
    public int getType()      { return type; }
    public int getRotation()  { return rotation; }
    
    public void setX(int x)   { this.x = x; }
    public void setY(int y)   { this.y = y; }

    // ============================================================
    // MOVEMENT — Board sẽ check collision trước khi gọi các hàm này
    // ============================================================
    public void moveLeft()  { x--; }
    public void moveRight() { x++; }
    public void moveDown()  { y++; }

    public void rotateClockwise() {
        rotation = (rotation + 1) % 4;
    }

    public void rotateCounterClockwise() {
        rotation = (rotation + 3) % 4;
    }

    public void rotate180() {
        rotation = (rotation + 2) % 4;
    }

    // Dùng khi cần thử di chuyển/xoay rồi rollback nếu collision
    public Tetromino copy() {
        Tetromino t = new Tetromino(this.type);
        t.rotation  = this.rotation;
        t.x         = this.x;
        t.y         = this.y;
        return t;
    }
}