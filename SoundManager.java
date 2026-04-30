import java.net.URL;
import javax.sound.sampled.Clip;

public class SoundManager {
    Clip musiClip;
    URL url[] = new URL[10];

    public SoundManager(){
        url[0] = getClass().getResource("/sound/music.wav");
        url[1] = getClass().getResource("/sound/coin.wav");
        url[2] = getClass().getResource("/sound/powerup.wav");
        url[3] = getClass().getResource("/sound/unlock.wav");
        url[4] = getClass().getResource("/sound/fanfare.wav");
    }
}
