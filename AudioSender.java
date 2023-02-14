/*
 * AudioSender.java
*/
 
import java.net.*;
import java.io.*;
import java.util.Properties;
import CMPC3M06.AudioRecorder;

public class AudioSender {
    static DatagramSocket sending_socket;
    static InetAddress clientIP;
    static AudioRecorder recorder;

    public static void main(String[] args) throws Exception {
        // Get config
        Properties prop = Config.get();

        // Port to send to
        int port = Integer.parseInt(prop.getProperty("port"));
        // IP address to send to
        try {
            clientIP = InetAddress.getByName(prop.getProperty("clientIP"));
        } catch (UnknownHostException e) {
            System.out.println("ERROR: AudioSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        // Open socket
        try {
            sending_socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        // Main loop
        recorder = new AudioRecorder();
        System.out.println("Sending audio...");
        while (true) try {
            // Get a block from recorder
            byte[] block = recorder.getBlock();
            // Make a DatagramPacket from it, with client address and port number, then send it
            DatagramPacket packet = new DatagramPacket(block, block.length, clientIP, port);
            sending_socket.send(packet);
            //System.out.println("Sent packet");
        } catch (IOException e) {
            System.out.println("ERROR: AudioSender: IO error occurred!");
            e.printStackTrace();
            break;
        }

       //Close the socket
       sending_socket.close();
    }
}
