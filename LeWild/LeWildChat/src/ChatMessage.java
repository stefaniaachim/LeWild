/**
 * Created by mariuspenteliuc on 10.5.16.
 */
import javax.sound.sampled.*;
import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server.
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    // The different types of message sent by the Client
    // WHOISIN to receive the list of the users connected
    // MESSAGE an ordinary message
    // LOGOUT to disconnect from the Server
    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
    private int type;
    private String message;

    // constructor
    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    // getters
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
