import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

        private static final String GENERATED_BGM_FILE = "bgm_generated.wav";

    private static final List<String> BGM_CANDIDATES = Arrays.asList(
            "bgm.wav",
            GENERATED_BGM_FILE,
            "bgm.mp3",
            "Menu - Tetris 99 OST.mp3"
    );

    private static final List<String> GAME_OVER_BGM_CANDIDATES = Arrays.asList(
            "gameover_theme.wav",
            "gameover_theme.mp3"
    );

    // ============================================================
    // FILE MAPPING — map key logic -> tên file thực tế trong thư mục Sound/
    // ============================================================
    private static final Map<String, List<String>> SOUND_FILES_BY_KEY = new HashMap<>();
    static {
        SOUND_FILES_BY_KEY.put(MOVE, Arrays.asList("move.wav", "touch floor.wav"));
        SOUND_FILES_BY_KEY.put(ROTATE, Arrays.asList("rotate.wav", "rotation.wav"));
        SOUND_FILES_BY_KEY.put(PLACE, Arrays.asList("place.wav", "touch floor.wav"));
        SOUND_FILES_BY_KEY.put(CLEAR, Arrays.asList("clear.wav", "delete line.wav"));
        SOUND_FILES_BY_KEY.put(TETRIS, Arrays.asList("tetris.wav", "clear.wav", "delete line.wav"));
        SOUND_FILES_BY_KEY.put(LEVEL_UP, Arrays.asList("levelup.wav", "level up.wav", "delete line.wav"));
        SOUND_FILES_BY_KEY.put(GAME_OVER, Arrays.asList("gameover.wav"));
    }

    // ============================================================
    // SINGLETON
    // ============================================================
    private static SoundManager instance;
    private Clip bgmClip;
    private AudioInputStream bgmStream;
    private Object fxBgmPlayer;
    private Clip gameOverClip;
    private AudioInputStream gameOverStream;

    private boolean bgmMuted = false;
    private boolean sfxMuted = false;
    private float bgmGainDb = -8.0f;
    private float sfxGainDb = -4.0f;
    private String bgmStatus = "Unavailable";
    private final Map<String, String> resolvedSfxByKey = new HashMap<>();

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    private SoundManager() {}

    // ============================================================
    // PLAY
    // ============================================================
    public void play(String soundKey) {
        if (sfxMuted) return;

        String fileName = resolveSfxFile(soundKey);
        if (fileName == null) {
            playSynthTone(soundKey);
            return;
        }

        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = loadAudioInputStream(fileName);
            if (ais == null) return;

            clip.open(ais);
            applyGain(clip, sfxGainDb);
            clip.start();

            // Tự giải phóng clip + stream sau khi phát xong
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        ais.close();
                    } catch (IOException ignored) {
                    }
                }
            });

        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException ex) {
            // Sound không critical — fail silently
        }
    }

    public int getAvailableSfxCount() {
        int count = 0;
        for (String key : SOUND_FILES_BY_KEY.keySet()) {
            if (resolveSfxFile(key) != null) count++;
        }
        return count;
    }

    public Map<String, String> getSfxStatusMap() {
        LinkedHashMap<String, String> status = new LinkedHashMap<>();
        status.put("MOVE", getSfxSourceLabel(MOVE));
        status.put("ROTATE", getSfxSourceLabel(ROTATE));
        status.put("PLACE", getSfxSourceLabel(PLACE));
        status.put("CLEAR", getSfxSourceLabel(CLEAR));
        status.put("TETRIS", getSfxSourceLabel(TETRIS));
        status.put("LEVEL", getSfxSourceLabel(LEVEL_UP));
        status.put("GAME OVER", getSfxSourceLabel(GAME_OVER));
        return status;
    }

    public void startBackgroundMusic() {
        if (bgmMuted) {
            bgmStatus = "Muted";
            return;
        }
        if ((bgmClip != null && bgmClip.isRunning()) || fxBgmPlayer != null) return;

        stopBackgroundMusic();
        ensureGeneratedFallbackBgm();

        for (String candidate : BGM_CANDIDATES) {
            if (candidate.toLowerCase().endsWith(".mp3")) {
                if (tryStartMp3BackgroundMusic(candidate)) {
                    bgmStatus = "MP3(JavaFX): " + candidate;
                    return;
                }
                continue;
            }

            try {
                AudioInputStream stream = loadAudioInputStream(candidate);
                if (stream == null) continue;

                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                applyGain(clip, bgmGainDb);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();

                bgmClip = clip;
                bgmStream = stream;
                bgmStatus = "WAV: " + candidate;
                return;
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                // thử candidate tiếp theo
            }
        }

        bgmStatus = "Unavailable";
    }

    public void stopBackgroundMusic() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }

        if (bgmStream != null) {
            try {
                bgmStream.close();
            } catch (IOException ignored) {
            }
            bgmStream = null;
        }

        stopFxBackgroundMusic();
        if (!bgmMuted) bgmStatus = "Stopped";
    }

    public void startGameOverMusic() {
        stopGameOverMusic();

        for (String candidate : GAME_OVER_BGM_CANDIDATES) {
            try {
                AudioInputStream stream = loadAudioInputStream(candidate);
                if (stream == null) continue;

                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                applyGain(clip, bgmGainDb);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();

                gameOverClip = clip;
                gameOverStream = stream;
                return;
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                // thử candidate tiếp theo
            }
        }

        // Fallback nếu không có nhạc game over riêng
        play(GAME_OVER);
    }

    public void stopGameOverMusic() {
        if (gameOverClip != null) {
            gameOverClip.stop();
            gameOverClip.close();
            gameOverClip = null;
        }

        if (gameOverStream != null) {
            try {
                gameOverStream.close();
            } catch (IOException ignored) {
            }
            gameOverStream = null;
        }
    }

    public void toggleBackgroundMusicMute() {
        bgmMuted = !bgmMuted;
        if (bgmMuted) {
            bgmStatus = "Muted";
            stopBackgroundMusic();
        }
        else startBackgroundMusic();
    }

    public void toggleSfxMute() {
        sfxMuted = !sfxMuted;
    }

    public boolean isBackgroundMusicMuted() {
        return bgmMuted;
    }

    public boolean isSfxMuted() {
        return sfxMuted;
    }

    public float getBackgroundMusicGainDb() {
        return bgmGainDb;
    }

    public float getSfxGainDb() {
        return sfxGainDb;
    }

    public int getBackgroundMusicVolumePercent() {
        return gainDbToPercent(bgmGainDb);
    }

    public int getSfxVolumePercent() {
        return gainDbToPercent(sfxGainDb);
    }

    public String getBackgroundMusicStatus() {
        return bgmStatus;
    }

    public void changeBackgroundMusicVolume(float deltaDb) {
        bgmGainDb = clampGain(bgmGainDb + deltaDb);
        if (bgmClip != null) applyGain(bgmClip, bgmGainDb);
        if (gameOverClip != null) applyGain(gameOverClip, bgmGainDb);
        applyFxBackgroundMusicVolume();
    }

    public void changeSfxVolume(float deltaDb) {
        sfxGainDb = clampGain(sfxGainDb + deltaDb);
    }

    private float clampGain(float value) {
        return Math.max(-40.0f, Math.min(6.0f, value));
    }

    private int gainDbToPercent(float gainDb) {
        float min = -40.0f;
        float max = 6.0f;
        float normalized = (gainDb - min) / (max - min);
        return Math.round(normalized * 100.0f);
    }

    private void applyGain(Clip clip, float gainDb) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float value = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gainDb));
            gainControl.setValue(value);
        } catch (IllegalArgumentException ignored) {
            // Một số system audio không hỗ trợ MASTER_GAIN
        }
    }

    private boolean tryStartMp3BackgroundMusic(String fileName) {
        File file = resolveFile(fileName);
        if (file == null) return false;

        try {
            // Khởi tạo JavaFX runtime nếu có sẵn trên máy
            Class.forName("javafx.embed.swing.JFXPanel")
                    .getDeclaredConstructor()
                    .newInstance();

            Class<?> platformClass = Class.forName("javafx.application.Platform");
            java.lang.reflect.Method runLater = platformClass.getMethod("runLater", Runnable.class);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean success = new AtomicBoolean(false);
            AtomicReference<Object> playerRef = new AtomicReference<>();

            runLater.invoke(null, new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
                        Object media = mediaClass
                                .getConstructor(String.class)
                                .newInstance(file.toURI().toString());

                        Class<?> playerClass = Class.forName("javafx.scene.media.MediaPlayer");
                        Object player = playerClass
                                .getConstructor(mediaClass)
                                .newInstance(media);

                        playerClass.getMethod("setCycleCount", int.class)
                                .invoke(player, Integer.MAX_VALUE);
                        playerClass.getMethod("setVolume", double.class)
                                .invoke(player, gainDbToLinear(bgmGainDb));
                        playerClass.getMethod("play").invoke(player);

                        playerRef.set(player);
                        success.set(true);
                    } catch (Throwable ignored) {
                    } finally {
                        latch.countDown();
                    }
                }
            });

            if (!latch.await(3, TimeUnit.SECONDS)) return false;
            if (!success.get()) return false;

            fxBgmPlayer = playerRef.get();
            return fxBgmPlayer != null;
        } catch (Throwable ex) {
            return false;
        }
    }

    private void stopFxBackgroundMusic() {
        if (fxBgmPlayer == null) return;

        Object playerToStop = fxBgmPlayer;
        fxBgmPlayer = null;

        try {
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            java.lang.reflect.Method runLater = platformClass.getMethod("runLater", Runnable.class);
            runLater.invoke(null, new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<?> playerClass = Class.forName("javafx.scene.media.MediaPlayer");
                        playerClass.getMethod("stop").invoke(playerToStop);
                        playerClass.getMethod("dispose").invoke(playerToStop);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void applyFxBackgroundMusicVolume() {
        if (fxBgmPlayer == null) return;

        Object playerToUpdate = fxBgmPlayer;
        try {
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            java.lang.reflect.Method runLater = platformClass.getMethod("runLater", Runnable.class);
            runLater.invoke(null, new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<?> playerClass = Class.forName("javafx.scene.media.MediaPlayer");
                        playerClass.getMethod("setVolume", double.class)
                                .invoke(playerToUpdate, gainDbToLinear(bgmGainDb));
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private double gainDbToLinear(float gainDb) {
        double linear = Math.pow(10.0, gainDb / 20.0);
        return Math.max(0.0, Math.min(1.0, linear));
    }

    private String resolveSfxFile(String soundKey) {
        String cached = resolvedSfxByKey.get(soundKey);
        if (cached != null && audioResourceExists(cached)) {
            return cached;
        }

        List<String> candidates = SOUND_FILES_BY_KEY.get(soundKey);
        if (candidates == null || candidates.isEmpty()) {
            candidates = new ArrayList<>();
            candidates.add(soundKey + ".wav");
        }

        for (String candidate : candidates) {
            if (audioResourceExists(candidate)) {
                resolvedSfxByKey.put(soundKey, candidate);
                return candidate;
            }
        }

        resolvedSfxByKey.remove(soundKey);
        return null;
    }

    private String getSfxSourceLabel(String soundKey) {
        String file = resolveSfxFile(soundKey);
        if (file != null) return "FILE: " + file;
        return "SYNTH";
    }

    private boolean audioResourceExists(String fileName) {
        if (getClass().getResource("/Sound/" + fileName) != null) return true;
        if (getClass().getResource("/sounds/" + fileName) != null) return true;

        File fsUpper = new File("Sound", fileName);
        if (fsUpper.exists()) return true;

        File fsLower = new File("sounds", fileName);
        return fsLower.exists();
    }

    private void playSynthTone(String soundKey) {
        double frequency;
        double durationSec;

        switch (soundKey) {
            case MOVE -> {
                frequency = 620.0;
                durationSec = 0.05;
            }
            case ROTATE -> {
                frequency = 760.0;
                durationSec = 0.06;
            }
            case PLACE -> {
                frequency = 300.0;
                durationSec = 0.07;
            }
            case CLEAR -> {
                frequency = 520.0;
                durationSec = 0.10;
            }
            case TETRIS -> {
                frequency = 880.0;
                durationSec = 0.16;
            }
            case LEVEL_UP -> {
                frequency = 980.0;
                durationSec = 0.20;
            }
            case GAME_OVER -> {
                frequency = 180.0;
                durationSec = 0.25;
            }
            default -> {
                frequency = 500.0;
                durationSec = 0.08;
            }
        }

        int sampleRate = 22050;
        int totalSamples = Math.max(1, (int) (sampleRate * durationSec));
        byte[] pcm = new byte[totalSamples * 2];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / sampleRate;
            double env = Math.max(0.0, 1.0 - (double) i / totalSamples);
            short sample = (short) (Math.sin(2.0 * Math.PI * frequency * t) * env * 4000);
            pcm[i * 2] = (byte) (sample & 0xFF);
            pcm[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        try {
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(pcm), format, totalSamples);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            applyGain(clip, sfxGainDb);
            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        ais.close();
                    } catch (IOException ignored) {
                    }
                }
            });
        } catch (LineUnavailableException | IOException ignored) {
            // Nếu synth tone cũng fail thì bỏ qua vì sound không critical.
        }
    }

    private File resolveFile(String fileName) {
        File fsUpper = new File("Sound", fileName);
        if (fsUpper.exists()) return fsUpper;

        File fsLower = new File("sounds", fileName);
        if (fsLower.exists()) return fsLower;

        return null;
    }

    private void ensureGeneratedFallbackBgm() {
        File existing = resolveFile(GENERATED_BGM_FILE);
        if (existing != null) return;

        File out = new File("Sound", GENERATED_BGM_FILE);
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            int sampleRate = 44100;
            double seconds = 12.0;
            int totalSamples = (int) (sampleRate * seconds);
            byte[] pcm = new byte[totalSamples * 2]; // 16-bit mono

            double[] notes = {
                    261.63, 329.63, 392.00, 523.25,
                    392.00, 329.63, 293.66, 392.00
            };
            double beat = 0.75; // seconds per note

            for (int i = 0; i < totalSamples; i++) {
                double t = (double) i / sampleRate;
                int noteIndex = (int) (t / beat) % notes.length;
                double freq = notes[noteIndex];

                // Envelope để đỡ gắt tai khi loop
                double noteTime = t % beat;
                double attack = Math.min(1.0, noteTime / 0.03);
                double release = Math.min(1.0, (beat - noteTime) / 0.10);
                double env = Math.min(attack, release);

                double tone = Math.sin(2.0 * Math.PI * freq * t);
                short sample = (short) (tone * env * 4500); // âm lượng vừa phải

                pcm[i * 2] = (byte) (sample & 0xFF);
                pcm[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            try (AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(pcm), format, totalSamples)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out);
            }
        } catch (Exception ignored) {
            // Nếu generate thất bại thì tiếp tục fallback theo candidates còn lại
        }
    }

    private AudioInputStream loadAudioInputStream(String fileName)
            throws UnsupportedAudioFileException, IOException {

        // 1) Ưu tiên classpath (nếu sau này đóng gói resource trong jar)
        URL classpathUpper = getClass().getResource("/Sound/" + fileName);
        if (classpathUpper != null) {
            return AudioSystem.getAudioInputStream(classpathUpper);
        }

        URL classpathLower = getClass().getResource("/sounds/" + fileName);
        if (classpathLower != null) {
            return AudioSystem.getAudioInputStream(classpathLower);
        }

        // 2) Fallback: đọc file trực tiếp từ thư mục dự án
        File fsUpper = new File("Sound", fileName);
        if (fsUpper.exists()) {
            return AudioSystem.getAudioInputStream(fsUpper);
        }

        File fsLower = new File("sounds", fileName);
        if (fsLower.exists()) {
            return AudioSystem.getAudioInputStream(fsLower);
        }

        return null;
    }
}
