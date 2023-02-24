import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Properties;
import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.*;
import javax.sound.sampled.LineUnavailableException;

public class VoiceSenderThread implements Runnable{

   static DatagramSocket sending_socket;
   static InetAddress clientIP;
   static AudioRecorder recorder;

   public void start()
   {
      Thread thread = new Thread(this);
      System.out.println("Recording Audio...");
      thread.start();
   }

   public void run () {
      // Port to send to
      int port = Config.getInt("port");
      // IP address to send to
      try {
         clientIP = InetAddress.getByName(Config.getString("clientIP"));
      } catch (UnknownHostException e) {
         System.out.println("ERROR: AudioSender: Could not find client IP");
         e.printStackTrace();
         System.exit(0);
      }
      // Open socket
      int socket = Config.getInt("socket");
      try {
         switch (socket) {
            case 1 -> sending_socket = new DatagramSocket(port);
            case 2 -> sending_socket = new DatagramSocket2(port);
            case 3 -> sending_socket = new DatagramSocket3(port);
            case 4 -> sending_socket = new DatagramSocket4(port);
            default -> throw new SocketException(socket + " is not a valid socket number");
         }
      } catch (SocketException e) {
         System.out.println("ERROR: AudioSender: Could not open UDP socket to send from.");
         e.printStackTrace();
         System.exit(0);
      }

      // Main loop
      try {
         recorder = new AudioRecorder();
      } catch (LineUnavailableException e) {
         throw new RuntimeException(e);
      }
      System.out.println("Sending audio...");

      // Sequence Numbering
      // **********************************************************************************
      int sequence_num = 0;
      while (true) try {
         // Get a block from recorder
         byte[] block = recorder.getBlock();
         // Convert the current sequence number into a byte array of size 8 and increment sequence number.
         byte[] byte_seq_num = ByteBuffer.allocate(8).putInt(sequence_num++).array();
         // Create the payload of size 520 in the format: [sequence number (8 bytes), and audio block (512 bytes)]
         byte[] payload = new byte[byte_seq_num.length + block.length];
         ByteBuffer buffer = ByteBuffer.wrap(payload);
         buffer.put(byte_seq_num);
         buffer.put(block);
         payload = buffer.array();

         // Make a DatagramPacket from it, with client address and port number, then send it
         DatagramPacket packet = new DatagramPacket(payload, payload.length, clientIP, port);
         // **********************************************************************************
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