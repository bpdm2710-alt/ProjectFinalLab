import java.awt.Color;

public class Board {

    // ============================================================
    // CONSTANTS
    // ============================================================
    public static final int COLS = 10;
    public static final int ROWS = 20;

    // ============================================================
    // GRID — null = ô trống, có Color = ô đã bị chiếm
    // ============================================================
    private Color[][] grid;

    public Board() {
        grid = new Color[ROWS][COLS];
    }

    // ============================================================
    // COLLISION DETECTION
    // ============================================================
    public boolean isValidPosition(Tetromino t) {
        int[][] shape = t.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;

                int boardX = t.getX() + col;
                int boardY = t.getY() + row;

                // Ra ngoài biên trái/phải/dưới
                if (boardX < 0 || boardX >= COLS || boardY >= ROWS) return false;

                // Trên trời (y < 0) vẫn cho phép — khối đang spawn
                if (boardY < 0) continue;

                // Ô đã bị chiếm
                if (grid[boardY][boardX] != null) return false;
            }
        }
        return true;
    }

    // ============================================================
    // PLACE TETROMINO — gọi khi khối không thể moveDown thêm
    // ============================================================
    public void place(Tetromino t) {
        int[][] shape = t.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;
                int boardX = t.getX() + col;
                int boardY = t.getY() + row;
                if (boardY >= 0) {
                    grid[boardY][boardX] = t.getColor();
                }
            }
        }
    }

    /**
     * Thử xoay clockwise với SRS Wall Kick.
     * Trả về Tetromino đã xoay+kick thành công, hoặc null nếu không thể xoay.
     */
    public Tetromino tryRotateCW(Tetromino t) {
        return tryRotate(t, 1);
    }

    public Tetromino tryRotateCCW(Tetromino t) {
        return tryRotate(t, -1); // -1 => +3 mod 4
    }

    public Tetromino tryRotate180(Tetromino t) {
        // 180 = CW hai lần, dùng kick của bước đầu
        Tetromino mid = tryRotate(t, 1);
        if (mid == null) return null;
        return tryRotate(mid, 1);
    }

    private Tetromino tryRotate(Tetromino t, int direction) {
        Tetromino probe = t.copy();
        if (direction == 1)       probe.rotateClockwise();
        else if (direction == -1) probe.rotateCounterClockwise();

        // Thử offset {0,0} trước
        if (isValidPosition(probe)) return probe;

        // Lấy kick data của trạng thái GỐC (trước khi xoay)
        int fromRotation = t.getRotation();
        // CCW: kick data theo chiều ngược — lấy từ rotation đích rồi đảo dấu
        int kickIndex = (direction == 1)
                ? fromRotation
                : ((fromRotation + 3) % 4);

        int[][][] kickData = t.getWallKickData();
        if (kickData == null) return null; // Khối O

        for (int[] offset : kickData[kickIndex]) {
            int dx = (direction == 1) ? offset[0] : -offset[0];
            int dy = (direction == 1) ? offset[1] : -offset[1];

            Tetromino kicked = probe.copy();
            kicked.setX(kicked.getX() + dx);
            kicked.setY(kicked.getY() + dy);

            if (isValidPosition(kicked)) return kicked;
        }
        return null; // Tất cả kick đều fail
    }
    // ============================================================
    // CLEAR LINES — trả về số dòng đã xóa (dùng để tính điểm)
    // ============================================================
    public int clearLines() {
        int cleared = 0;
        for (int row = ROWS - 1; row >= 0; row--) {
            if (isLineFull(row)) {
                removeLine(row);
                cleared++;
                row++; // kiểm tra lại cùng row vì các dòng trên đã rơi xuống
            }
        }
        return cleared;
    }

    private boolean isLineFull(int row) {
        for (int col = 0; col < COLS; col++) {
            if (grid[row][col] == null) return false;
        }
        return true;
    }

    private void removeLine(int targetRow) {
        // Dòng targetRow bị xóa, các dòng trên rơi xuống 1 hàng
        for (int row = targetRow; row > 0; row--) {
            grid[row] = grid[row - 1];
        }
        // Dòng trên cùng trở thành trống
        grid[0] = new Color[COLS];
    }


    // ============================================================
    // GAME OVER — khối spawn mà không có chỗ
    // ============================================================
    public boolean isGameOver(Tetromino t) {
        return !isValidPosition(t);
    }

    // ============================================================
    // GHOST PIECE — tính vị trí drop thấp nhất
    // ============================================================
    public Tetromino getGhost(Tetromino t) {
        Tetromino ghost = t.copy(); //
        while (true) {
            Tetromino probe = ghost.copy();
            probe.moveDown();
            if (!isValidPosition(probe)) break;
            ghost.moveDown();
        }
        return ghost;
    }

    // ============================================================
    // GETTER — GamePanel dùng để vẽ
    // ============================================================
    public Color getCell(int row, int col) {
        return grid[row][col];
    }

    public void reset() {
        grid = new Color[ROWS][COLS];
    }
}