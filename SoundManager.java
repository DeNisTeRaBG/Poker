import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    
    // We run the sound on a separate lightweight thread so it never freezes the game UI
    public static void play(String fileName) {
        new Thread(() -> {
            try {
                File soundFile = new File("assets/sfx/" + fileName);
                if (!soundFile.exists()) {
                    System.out.println("Sound file missing: " + fileName);
                    return;
                }
                
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                // Allow the thread to live long enough for the sound to finish playing
                // (Assuming no sound effect is longer than 5 seconds)
                Thread.sleep(5000); 
                clip.close();
                
            } catch (Exception e) {
                System.out.println("Error playing sound: " + fileName);
                e.printStackTrace();
            }
        }).start();
    }
}