import java.awt.Color;

/**
 * ThemeManager — quản lý các theme (bộ màu) cho game
 * Cấp 4 theme: CLASSIC, DARK_MODERN, NEON, RETRO
 */
public class ThemeManager {

    public enum Theme {
        CLASSIC,
        DARK_MODERN,
        NEON,
        RETRO
    }

    private static Theme currentTheme = Theme.DARK_MODERN;

    // ============================================================
    // COLOR PALETTE — Các màu chính theo theme
    // ============================================================
    public static class ThemePalette {
        public Color background;
        public Color grid;
        public Color text;
        public Color textSecondary;
        public Color boardBackground;
        public Color sidebarBackground;
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static ThemePalette getPalette() {
        return switch (currentTheme) {
            case CLASSIC -> getClassicPalette();
            case DARK_MODERN -> getDarkModernPalette();
            case NEON -> getNeonPalette();
            case RETRO -> getRetroPalette();
        };
    }

    // ============================================================
    // CLASSIC — Nintendo Tetris style
    // ============================================================
    private static ThemePalette getClassicPalette() {
        ThemePalette p = new ThemePalette();
        p.background = new Color(0, 0, 0);
        p.grid = new Color(100, 100, 100);
        p.text = new Color(200, 200, 200);
        p.textSecondary = new Color(150, 150, 150);
        p.boardBackground = new Color(0, 0, 0);
        p.sidebarBackground = new Color(20, 20, 20);
        return p;
    }

    // ============================================================
    // DARK_MODERN — Modern dark mode (tetr.io-like)
    // ============================================================
    private static ThemePalette getDarkModernPalette() {
        ThemePalette p = new ThemePalette();
        p.background = new Color(15, 15, 15);
        p.grid = new Color(40, 40, 40);
        p.text = new Color(255, 255, 255);
        p.textSecondary = new Color(160, 160, 160);
        p.boardBackground = new Color(0, 0, 0);
        p.sidebarBackground = new Color(20, 20, 20);
        return p;
    }

    // ============================================================
    // NEON — Cyberpunk/neon aesthetic
    // ============================================================
    private static ThemePalette getNeonPalette() {
        ThemePalette p = new ThemePalette();
        p.background = new Color(10, 5, 20);
        p.grid = new Color(0, 255, 200);  // Cyan neon
        p.text = new Color(0, 255, 200);  // Cyan neon
        p.textSecondary = new Color(255, 0, 200); // Magenta neon
        p.boardBackground = new Color(5, 5, 15);
        p.sidebarBackground = new Color(10, 5, 20);
        return p;
    }

    // ============================================================
    // RETRO — 8-bit retro style
    // ============================================================
    private static ThemePalette getRetroPalette() {
        ThemePalette p = new ThemePalette();
        p.background = new Color(30, 30, 30);
        p.grid = new Color(80, 80, 120);
        p.text = new Color(200, 200, 255);
        p.textSecondary = new Color(150, 150, 200);
        p.boardBackground = new Color(20, 20, 50);
        p.sidebarBackground = new Color(40, 40, 80);
        return p;
    }

    // ============================================================
    // TETROMINO COLORS — khối theo từng theme (giữ nguyên logic)
    // ============================================================
    public static Color getTetrominoColor(int type) {
        // Giữ nguyên màu gốc cho tetromino vì chúng đã hay rồi
        // và dễ nhận biết theo chuẩn Tetris
        Color[] colors = {
            new Color(0,   240, 240), // I — cyan
            new Color(240, 240,   0), // O — yellow
            new Color(160,   0, 240), // T — purple
            new Color(0,   240,   0), // S — green
            new Color(240,   0,   0), // Z — red
            new Color(0,     0, 240), // J — blue
            new Color(240, 160,   0)  // L — orange
        };
        return colors[type % colors.length];
    }

    // ============================================================
    // HELPER — kiểm tra theme hiện tại
    // ============================================================
    public static boolean isNeonTheme() {
        return currentTheme == Theme.NEON;
    }

    public static boolean isRetroTheme() {
        return currentTheme == Theme.RETRO;
    }
}
