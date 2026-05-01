import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GameState {

    // ============================================================
    // ENUM STATE
    // ============================================================
    public enum State {
        MENU, SETTINGS, PLAYING, PAUSED, GAME_OVER
    }

    // ============================================================
    // SCORING TABLE — điểm theo số dòng xóa 1 lần
    // Chuẩn Tetris guideline: 1=100, 2=300, 3=500, 4=800
    // ============================================================
    private static final int[] LINE_POINTS = {0, 100, 300, 500, 800};
    private static final String PREF_KEY_HIGH_SCORE = "highScore";

    // ============================================================
    // FIELDS
    // ============================================================
    private State currentState;
    private int score;
    private int highScore;
    private int level;
    private int totalLinesCleared;
    private int comboCount;
    private boolean lastIsTSpin;
    private int lastTSpinBonus;

    // Observer — Danh sách các listener (GamePanel, SidebarPanel, SoundManager...)
    private List<Runnable> listeners;
    private final Preferences preferences;

    public GameState() {
        listeners = new ArrayList<>();
        preferences = Preferences.userNodeForPackage(GameState.class);
        highScore = preferences.getInt(PREF_KEY_HIGH_SCORE, 0);
        reset();
    }

    // ============================================================
    // RESET — dùng khi bắt đầu game mới
    // ============================================================
    public void reset() {
        score = 0;
        level = 1;
        totalLinesCleared = 0;
        comboCount = 0;
        lastIsTSpin = false;
        lastTSpinBonus = 0;
        currentState = State.MENU;
    }

    // ============================================================
    // SCORING
    // ============================================================
    public void addLines(int linesCleared) {
        addLines(linesCleared, false);
    }

    public void addLines(int linesCleared, boolean isTSpin) {
        if (linesCleared <= 0) {
            comboCount = 0; // Reset combo if no lines cleared
            lastIsTSpin = false;
            lastTSpinBonus = 0;
            return;
        }
        
        int points = LINE_POINTS[Math.min(linesCleared, 4)] * level;
        
        // Combo bonus: extra 50 points per combo count
        int comboBonus = comboCount * 50;
        
        // T-Spin bonus: varies by lines cleared
        int tspinBonus = 0;
        if (isTSpin) {
            if (linesCleared == 1) tspinBonus = 400 * level;      // T-Spin Single
            else if (linesCleared == 2) tspinBonus = 800 * level;  // T-Spin Double
            else if (linesCleared >= 3) tspinBonus = 1200 * level; // T-Spin Triple
        }
        
        score += points + comboBonus + tspinBonus;
        
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
        
        totalLinesCleared += linesCleared;
        comboCount++;
        level = (totalLinesCleared / 10) + 1;
        lastIsTSpin = isTSpin;
        lastTSpinBonus = tspinBonus;
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

    public void notifyChanged() {
        for (Runnable r : listeners) {
            r.run();
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================
    public State getCurrentState() { return currentState; }
    public int getScore() { return score; }
    public int getHighScore() { return highScore; }
    public int getLevel() { return level; }
    public int getLinesCleared() { return totalLinesCleared; }
    public int getComboCount() { return comboCount; }
    public boolean getLastIsTSpin() { return lastIsTSpin; }
    public int getLastTSpinBonus() { return lastTSpinBonus; }
    
    public void clearTSpinFlag() {
        lastIsTSpin = false;
    }

    // Drop speed tính bằng ms — level càng cao càng nhanh
    public int getDropInterval() {
        return Math.max(100, 800 - (level - 1) * 70);
    }

    private void saveHighScore() {
        preferences.putInt(PREF_KEY_HIGH_SCORE, highScore);
        try {
            preferences.flush();
        } catch (BackingStoreException ignored) {
            // High score vẫn giữ trong RAM nếu không ghi được xuống disk.
        }
    }
}