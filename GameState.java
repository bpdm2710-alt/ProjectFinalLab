import java.util.ArrayList;
import java.util.List;

public class GameState {

    // ============================================================
    // ENUM STATE
    // ============================================================
    public enum State {
        MENU, PLAYING, PAUSED, GAME_OVER
    }

    // ============================================================
    // SCORING TABLE — điểm theo số dòng xóa 1 lần
    // Chuẩn Tetris guideline: 1=100, 2=300, 3=500, 4=800
    // ============================================================
    private static final int[] LINE_POINTS = {0, 100, 300, 500, 800};

    // ============================================================
    // FIELDS
    // ============================================================
    private State currentState;
    private int score;
    private int level;
    private int totalLinesCleared;

    // Observer — Danh sách các listener (GamePanel, SidebarPanel, SoundManager...)
    private List<Runnable> listeners;

    public GameState() {
        listeners = new ArrayList<>();
        reset();
    }

    // ============================================================
    // RESET — dùng khi bắt đầu game mới
    // ============================================================
    public void reset() {
        score = 0;
        level = 1;
        totalLinesCleared = 0;
        currentState = State.MENU;
    }

    // ============================================================
    // SCORING
    // ============================================================
    public void addLines(int linesCleared) {
        if (linesCleared <= 0) return;
        int points = LINE_POINTS[Math.min(linesCleared, 4)] * level;
        score += points;
        totalLinesCleared += linesCleared;
        level = (totalLinesCleared / 10) + 1;
        notifyChanged(); 
    }

    // ============================================================
    // STATE TRANSITIONS
    // ============================================================
    public void setState(State newState) {
        this.currentState = newState;
        notifyChanged();
    }

    public void togglePause() {
        if (currentState == State.PLAYING) setState(State.PAUSED);
        else if (currentState == State.PAUSED) setState(State.PLAYING);
    }

    // ============================================================
    // OBSERVER PATTERN — Đăng ký và thông báo
    // ============================================================
    public void addListener(Runnable callback) {
        listeners.add(callback);
    }

    private void notifyChanged() {
        for (Runnable r : listeners) {
            r.run();
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================
    public State getCurrentState() { return currentState; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getLinesCleared() { return totalLinesCleared; }

    // Drop speed tính bằng ms — level càng cao càng nhanh
    public int getDropInterval() {
        return Math.max(100, 800 - (level - 1) * 70);
    }
}