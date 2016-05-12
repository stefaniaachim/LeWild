import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.Socket;

public class Client {

    private ObjectInputStream sInput;       // to read from the socket
    private ObjectOutputStream sOutput;     // to write on the socket
    private Socket socket;

    private String server, username;
    private int port;

    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
        // save if we are in GUI mode or not
    }

    public static void main(String[] args) throws IOException {
        // default values
        int portNumber = 1500;// = 22003;
        String serverAddress = "localhost";// = "79.114.106.252";
        String userName = "";// = "MacBook";

        // depending of the number of arguments provided we fall through
        switch (args.length) {
            case 3:
                // > javac Client username portNumber
                serverAddress = args[2];
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                    break;
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                }
            case 1:
                userName = args[0];
                break;
            case 0: {
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Choose server IP address: ");
                serverAddress = bufferRead.readLine();
                System.out.println("Choose port number: ");
                portNumber = Integer.parseInt(bufferRead.readLine());
                System.out.println("Choose user name: ");
                userName = bufferRead.readLine();
                break;
            }
            // invalid number of arguments
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                return;
        }
        Client client = new Client(serverAddress, portNumber, userName);
        // test if we can start the connection to the Server
        // if it failed nothing we can do
        if (!client.start())
            return;

        // wait for messages from user
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        // loop forever for message from the user
        while (true) {
            System.out.print("> ");
            String msg = bufferRead.readLine();//;scan.nextLine();
            if (msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                break;
            } else if (msg.equalsIgnoreCase("WHOISIN")) {
                client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            } else {
                // default to ordinary message
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
            }
        }
        client.disconnect();
    }

    public static synchronized void playTheSound() {//final String url) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    InputStream inputStream = getClass().getResourceAsStream("gong.au");
                    AudioStream audioStream = new AudioStream(inputStream);
                    AudioPlayer.player.start(audioStream);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static synchronized void playOtherSound() {
        try {
            File f = new File(System.getProperty("user.dir") + "/src/audio.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            Thread.sleep(100);
            clip.drain();
            clip.close();
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean start() {
        try {
            socket = new Socket(server, port);
        } catch (Exception ec) {
            display("Error connecting to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

		/* Creating both Data Stream */
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // creates the Thread to listen from the server
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }

    private void display(String msg) {
        System.out.println(msg);
    }

    /*
     * To send a message to the server
     */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /**
     * When something goes wrong
     * Close the Input/Output streams and disconnect
     * not much to do in the catch clause
     */
    private void disconnect() {
        try {
            if (sInput != null) sInput.close();
            if (sOutput != null) sOutput.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {
            // not much else I can do
        }
    }

    class ListenFromServer extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    playOtherSound();
                    System.out.println(msg);
                    System.out.print("> ");
                } catch (IOException e) {
                    display("Server has close the connection: " + e);
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
    }
}
