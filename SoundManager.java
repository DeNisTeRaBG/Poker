import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    
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
                
                Thread.sleep(5000); 
                clip.close();
                
            } catch (Exception e) {
                System.out.println("Error playing sound: " + fileName);
                e.printStackTrace();
            }
        }).start();
    }
}