import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    // ============================================================
    // SOUND KEYS
    // ============================================================
    public static final String MOVE       = "move";
    public static final String ROTATE     = "rotate";
    public static final String PLACE      = "place";
    public static final String CLEAR      = "clear";
    public static final String TETRIS     = "tetris";
    public static final String LEVEL_UP   = "levelup";
    public static final String GAME_OVER  = "gameover";

    // ============================================================
    // SINGLETON
    // ============================================================
    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    private SoundManager() {}

    // ============================================================
    // PLAY
    // ============================================================
    public void play(String soundKey) {
        String path = "/sounds/" + soundKey + ".wav";
        try {
            URL url = getClass().getResource(path);
            if (url == null) return; // file chưa có, bỏ qua

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();

            // Tự giải phóng sau khi phát xong
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });

        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException ex) {
            // Sound không critical — fail silently
        }
    }
}