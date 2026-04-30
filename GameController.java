import javax.swing.Timer;
import java.awt.event.KeyEvent;

public class GameController {
    private final int DAS = 130; // Thời gian chờ kích hoạt trượt (ms)
    private final int ARR = 0;   // Tốc độ lặp (0 = lướt tức thời đến sát tường)
    private final int SDF = 40;  // Soft Drop Factor: Tốc độ rơi nhanh gấp 40 lần
    // ============================================================
    // CẤU HÌNH LOCK DELAY (INFINITY RULE)
    // ============================================================
    private final int LOCK_DELAY = 500; 
    private final int MAX_LOCK_RESETS = 15;
    
    private boolean isLocking = false;
    private long lockStartTime = 0;
    private int lockResets = 0;
    // Input States
    private boolean leftPressed, rightPressed, downPressed;
    private long leftPressTime, rightPressTime, downPressTime;
    private long lastArrTime, lastSdfTime;
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
        initInputLoop(); // KÍCH HOẠT LUỒNG ĐỌC PHÍM SIÊU TỐC
    }

    // ============================================================
    // GAME LOOP (Thread-safe via Swing Timer)
    // ============================================================
    private void initInputLoop() {
        // Luồng chạy nền, lặp mỗi 1 mili-giây để check phím
        new Thread(() -> {
            while (true) {
                if (state.getCurrentState() == GameState.State.PLAYING) {
                    processInput();
                }
                try { Thread.sleep(1); } catch (InterruptedException e) {}
            }
        }).start();
    }

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
        current = factory.next();
        holdUsed = false;
        
        // Reset toàn bộ trạng thái Lock cho gạch mới
        isLocking = false;
        lockResets = 0; 
        
        if (board.isGameOver(current)) {
            state.setState(GameState.State.GAME_OVER);
            stop();
        }
        // [THÊM DÒNG NÀY] Báo cho SidebarPanel vẽ lại ô Next Piece
        state.notifyChanged();
    }

    private void moveDown() {
        Tetromino probe = current.copy();
        probe.moveDown();
        
        if (board.isValidPosition(probe)) {
            current.moveDown();
            // Nếu gạch đang bị kẹt mà lại trượt rớt xuống vực -> Hủy trạng thái Lock
            if (isLocking) {
                isLocking = false;
            }
        } else {
            // Chạm đáy -> Kích hoạt đếm ngược 500ms
            if (!isLocking) {
                isLocking = true;
                lockStartTime = System.currentTimeMillis();
            }
        }
    }

    private void lockPiece() {
        board.place(current);
        int lines = board.clearLines();
        
        SoundManager.getInstance().play(SoundManager.PLACE);
        if (lines == 4) SoundManager.getInstance().play(SoundManager.TETRIS);
        else if (lines > 0) SoundManager.getInstance().play(SoundManager.CLEAR);
        
        int oldLevel = state.getLevel();
        state.addLines(lines);
        if (state.getLevel() > oldLevel) {
            SoundManager.getInstance().play(SoundManager.LEVEL_UP);
        }
        
        dropTimer.setDelay(state.getDropInterval());
        spawnNext();
    }
            
    // ============================================================
    // INPUT HANDLING
    // ============================================================
    private void processInput() {
        long now = System.currentTimeMillis();

        // 1. Soft Drop
        if (downPressed) {
            long dropInterval = state.getDropInterval() / SDF;
            if (now - lastSdfTime >= Math.max(1, dropInterval)) {
                moveDown(); 
                lastSdfTime = now;
            }
        }

        // 2. DAS & ARR (Trái / Phải)
        if (leftPressed && !rightPressed) {
            handleAutoShift(now, leftPressTime, KeyEvent.VK_LEFT);
        } else if (rightPressed && !leftPressed) {
            handleAutoShift(now, rightPressTime, KeyEvent.VK_RIGHT);
        }

        // 3. [THÊM MỚI] Giám sát Lock Delay
        if (isLocking && (now - lockStartTime >= LOCK_DELAY)) {
            lockPiece();
        }
    }

    private void handleAutoShift(long now, long pressTime, int direction) {
        if (now - pressTime >= DAS) {
            if (ARR == 0) {
                // ARR = 0: Trượt tức thời đến sát vách
                boolean moved = true;
                while (moved) {
                    moved = moveHorizontal(direction);
                }
            } else {
                // ARR > 0: Trượt từng ô theo nhịp
                if (now - lastArrTime >= ARR) {
                    moveHorizontal(direction);
                    lastArrTime = now;
                }
            }
        }
    }

    private void resetLockDelay() {
        // Chỉ reset nếu đang chạm đáy và chưa dùng hết 15 mạng
        if (isLocking && lockResets < MAX_LOCK_RESETS) {
            lockStartTime = System.currentTimeMillis();
            lockResets++;
        }
    }

    private boolean moveHorizontal(int direction) {
        Tetromino probe = current.copy();
        if (direction == KeyEvent.VK_LEFT) probe.moveLeft();
        else probe.moveRight();

        if (board.isValidPosition(probe)) {
            if (direction == KeyEvent.VK_LEFT) current.moveLeft();
            else current.moveRight();
            
            resetLockDelay(); // Hồi lại 500ms
            return true;
        }
        return false;
    }

    public void keyPressed(int keyCode) {
        if (state.getCurrentState() == GameState.State.GAME_OVER && keyCode == KeyEvent.VK_R) {
            board.reset(); factory.reset(); state.reset();
            spawnNext(); start();
            return;
        }
        if (keyCode == KeyEvent.VK_P) { state.togglePause(); return; }
        if (state.getCurrentState() != GameState.State.PLAYING) return;

        Tetromino probe = current.copy();
        long now = System.currentTimeMillis();

        switch (keyCode) {
            case KeyEvent.VK_LEFT -> {
                if (!leftPressed) { leftPressed = true; leftPressTime = now; moveHorizontal(keyCode); SoundManager.getInstance().play(SoundManager.MOVE); }
            }
            case KeyEvent.VK_RIGHT -> {
                if (!rightPressed) { rightPressed = true; rightPressTime = now; moveHorizontal(keyCode); SoundManager.getInstance().play(SoundManager.MOVE); }
            }
            case KeyEvent.VK_DOWN -> {
                if (!downPressed) { downPressed = true; lastSdfTime = now; moveDown(); }
            }
            case KeyEvent.VK_UP, KeyEvent.VK_X -> {
                Tetromino rotated = board.tryRotateCW(current);
                if (rotated != null) {
                    current = rotated;
                    SoundManager.getInstance().play(SoundManager.ROTATE);
                    resetLockDelay();
                }
            }

            case KeyEvent.VK_Z -> {
                Tetromino rotated = board.tryRotateCCW(current);
                if (rotated != null) {
                    current = rotated;
                    SoundManager.getInstance().play(SoundManager.ROTATE);
                    resetLockDelay();
                }
            }

            case KeyEvent.VK_A -> {
                Tetromino rotated = board.tryRotate180(current);
                if (rotated != null) {
                    current = rotated;
                    SoundManager.getInstance().play(SoundManager.ROTATE);
                    resetLockDelay();
                }
            }
            case KeyEvent.VK_SPACE -> {
                current = board.getGhost(current);
                lockPiece(); // Hard drop thì khóa tử hình ngay lập tức, không khoan nhượng!
            }
            case KeyEvent.VK_C -> holdPiece();
        }
    }

    public void keyReleased(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT  -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_DOWN  -> downPressed = false;
        }
    }

    private void hardDrop() {
        // Dịch chuyển khối hiện tại xuống tận vị trí của bóng mờ (ghost)
        current = board.getGhost(current);
        moveDown(); // Gọi moveDown() để thực hiện logic khóa gạch, xóa hàng và phát âm thanh
        resetLockDelay(); // Hồi lại 500ms
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
            // [THÊM DÒNG NÀY] Báo cho SidebarPanel vẽ lại ô Hold Piece
            state.notifyChanged();
        }
        holdUsed = true;
    }

    // ============================================================
    // GETTERS — GamePanel và SidebarPanel dùng để vẽ GUI
    // ============================================================
    public Tetromino getCurrent() { return current; }
    public Tetromino getHeld()    { return held; }
}