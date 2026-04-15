import java.io.File;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.sound.sampled.*;

public class SoundManager {

    private static Clip bgClip;
    private static final HashMap<String, Clip> cache = new HashMap<>();

    private static boolean soundOn = true;
    private static boolean musicOn = true;
    private static float volume = -10f;

    private static final Preferences prefs =
            Preferences.userNodeForPackage(SoundManager.class);

    static {
        // Load saved settings
        soundOn = prefs.getBoolean("soundOn", true);
        musicOn = prefs.getBoolean("musicOn", true);
        volume = prefs.getFloat("volume", -10f);
    }

    // ================= LOAD CLIP (CACHE) =================
    private static Clip getClip(String path) {
        try {
            if (cache.containsKey(path)) {
                return cache.get(path);
            }

            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);

            cache.put(path, clip);
            return clip;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================= PLAY SOUND =================
    public static void playSound(String path) {
        if (!soundOn) return;

        try {
            Clip clip = getClip(path);
            if (clip == null) return;

            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);

            setClipVolume(clip);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MUSIC =================
    public static void playMusicLoop(String path) {
        if (!musicOn) return;

        try {
            if (bgClip != null && bgClip.isRunning()) return;

            bgClip = getClip(path);
            if (bgClip == null) return;

            bgClip.setFramePosition(0);
            setClipVolume(bgClip);
            bgClip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (bgClip != null) {
            bgClip.stop();
        }
    }

    // ================= VOLUME =================
    private static void setClipVolume(Clip clip) {
        try {
            FloatControl gain =
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(volume);
        } catch (Exception ignored) {}
    }

    public static void setVolume(int percent) {
        if (percent == 0) {
            volume = -80f;
        } else {
            volume = (float) (Math.log10(percent / 100.0) * 20.0);
        }

        prefs.putFloat("volume", volume);

        if (bgClip != null) {
            setClipVolume(bgClip);
        }
    }

    // ================= TOGGLE =================
    public static void setSound(boolean on) {
        soundOn = on;
        prefs.putBoolean("soundOn", on);
    }

    public static void setMusic(boolean on) {
        musicOn = on;
        prefs.putBoolean("musicOn", on);

        if (!on){
            stopMusic();
        } 
        else{
            if (bgClip != null && !bgClip.isRunning()) {
                setClipVolume(bgClip); // Cập nhật lại volume hiện tại
                bgClip.loop(Clip.LOOP_CONTINUOUSLY); // Phát lặp lại
            }
        }
    }

    public static boolean isSoundOn() {
        return soundOn;
    }

    public static boolean isMusicOn() {
        return musicOn;
    }
}