import javax.sound.sampled.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

public class ChatMessage implements Serializable {

    // The different types of message sent by the Client
    // WHOISIN to receive the list of the users connected
    // MESSAGE an ordinary message
    // LOGOUT to disconnect from the Server
    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
    private int type;
    private String message;

    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }

    void playSound(File f) {
        Runnable r = new Runnable() {
            private File f;

            public void run() {
                playSoundInternal(this.f);
            }

            public Runnable setFile(File f) {
                this.f = f;
                return this;
            }
        }.setFile(f);

        new Thread(r).start();
    }

    private void playSoundInternal(File f) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(f);
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                try {
                    clip.start();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    clip.drain();
                } finally {
                    clip.close();
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } finally {
                audioInputStream.close();
            }
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
