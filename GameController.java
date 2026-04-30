import javax.swing.Timer;
import java.awt.event.KeyEvent;

public class GameController {

    private Board            board;
    private TetrominoFactory factory;
    private GameState        state;
    
    private Tetromino        current;
    private Tetromino        held;
    private boolean          holdUsed;
    private Timer            dropTimer;

    public GameController(Board board, TetrominoFactory factory, GameState state) {
        this.board   = board;
        this.factory = factory;
        this.state   = state;
        
        spawnNext();
        initTimer();
    }

    // ============================================================
    // GAME LOOP (Thread-safe via Swing Timer)
    // ============================================================
    private void initTimer() {
        dropTimer = new Timer(state.getDropInterval(), e -> {
            if (state.getCurrentState() == GameState.State.PLAYING) {
                moveDown();
            }
        });
    }

    public void start() {
        state.setState(GameState.State.PLAYING);
        dropTimer.start();
    }

    public void stop() {
        dropTimer.stop();
    }

    // ============================================================
    // CORE LOGIC: SPAWN & MOVEMENT
    // ============================================================
    private void spawnNext() {
        current  = factory.next();
        holdUsed = false;
        
        if (board.isGameOver(current)) {
            state.setState(GameState.State.GAME_OVER);
            stop();
        }
    }

    private void moveDown() {
        Tetromino probe = current.copy();
        probe.moveDown();
        
        if (board.isValidPosition(probe)) {
            current.moveDown();
        } else {
            // Khối gạch chạm đáy/chạm gạch khác -> Đóng băng
            board.place(current);
            int lines = board.clearLines();
            
            // 1. Âm thanh chạm đáy
            SoundManager.getInstance().play(SoundManager.PLACE);
            
            // 2. Âm thanh xóa dòng
            if (lines == 4) {
                SoundManager.getInstance().play(SoundManager.TETRIS);
            } else if (lines > 0) {
                SoundManager.getInstance().play(SoundManager.CLEAR);
            }
            
            // 3. Tính điểm và check Level Up
            int oldLevel = state.getLevel();
            state.addLines(lines);
            if (state.getLevel() > oldLevel) {
                SoundManager.getInstance().play(SoundManager.LEVEL_UP);
            }
            
            // Cập nhật lại tốc độ rơi nếu có tăng level
            dropTimer.setDelay(state.getDropInterval());
            spawnNext();
        }
    }

    // ============================================================
    // INPUT HANDLING
    // ============================================================
    public void handleKey(int keyCode) {
        // Restart từ GAME_OVER
        if (keyCode == KeyEvent.VK_R &&
            state.getCurrentState() == GameState.State.GAME_OVER) {
            board.reset();
            factory.reset();
            state.reset();
            spawnNext();
            start();
            return;
        }

        if (state.getCurrentState() == GameState.State.GAME_OVER) return;

        if (keyCode == KeyEvent.VK_P) {
            state.togglePause();
            return;
        }

        if (state.getCurrentState() != GameState.State.PLAYING) return;

        Tetromino probe = current.copy();
        
        switch (keyCode) {
            case KeyEvent.VK_LEFT  -> { 
                probe.moveLeft();                 
                if (board.isValidPosition(probe)) { 
                    current.moveLeft(); 
                    SoundManager.getInstance().play(SoundManager.MOVE); 
                } 
            }
            case KeyEvent.VK_RIGHT -> { 
                probe.moveRight();                
                if (board.isValidPosition(probe)) { 
                    current.moveRight(); 
                    SoundManager.getInstance().play(SoundManager.MOVE); 
                } 
            }
            case KeyEvent.VK_DOWN  -> moveDown(); // Soft drop
            case KeyEvent.VK_UP    -> { 
                probe.rotateClockwise();          
                if (board.isValidPosition(probe)) { 
                    current.rotateClockwise(); 
                    SoundManager.getInstance().play(SoundManager.ROTATE); 
                } 
            }
            case KeyEvent.VK_Z     -> { 
                probe.rotateCounterClockwise();   
                if (board.isValidPosition(probe)) { 
                    current.rotateCounterClockwise(); 
                    SoundManager.getInstance().play(SoundManager.ROTATE); 
                } 
            }
            case KeyEvent.VK_SPACE -> hardDrop();
            case KeyEvent.VK_C     -> holdPiece();
        }
    }

    private void hardDrop() {
        // Dịch chuyển khối hiện tại xuống tận vị trí của bóng mờ (ghost)
        current = board.getGhost(current);
        moveDown(); // Gọi moveDown() để thực hiện logic khóa gạch, xóa hàng và phát âm thanh
    }

    private void holdPiece() {
        if (holdUsed) return;
        
        if (held == null) {
            // Cất khối hiện tại đi và sinh khối mới
            held = new Tetromino(current.getType());
            spawnNext();
        } else {
            // Hoán đổi. Lệnh new Tetromino() đảm bảo khối được lôi ra 
            // sẽ tự động quay về x=3, y=0 và rotation=0 (Nguyên trạng ban đầu).
            Tetromino temp = new Tetromino(current.getType());
            current = new Tetromino(held.getType());
            held = temp;
        }
        holdUsed = true;
    }

    // ============================================================
    // GETTERS — GamePanel và SidebarPanel dùng để vẽ GUI
    // ============================================================
    public Tetromino getCurrent() { return current; }
    public Tetromino getHeld()    { return held; }
}