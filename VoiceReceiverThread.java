import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.util.Properties;

public class VoiceReceiverThread implements Runnable
{
   static DatagramSocket receiving_socket;
   static AudioPlayer player;

   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }

   public void run()
   {
      // Get config
      Properties prop = Config.get();

      // Port to receive on
      int port = Integer.parseInt(prop.getProperty("port"));

      // Open socket
      try {
         receiving_socket = new DatagramSocket(port);
      } catch (SocketException e){
         System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
         e.printStackTrace();
         System.exit(0);
      }

      // Main loop
      try {
         player = new AudioPlayer();
      } catch (LineUnavailableException e) {
         throw new RuntimeException(e);
      }
      System.out.println("Receiving audio...");
      while (true) try {
         // Receive a DatagramPacket
         byte[] buffer = new byte[512];
         DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

         receiving_socket.receive(packet);

         // Play data from the byte buffer
         player.playBlock(buffer);
         // System.out.println("Packet received");
      } catch (IOException e) {
         System.out.println("ERROR: AudioReceiver: IO error occurred!");
         e.printStackTrace();
         break;
      }

      // Close the socket
      receiving_socket.close();
   }
}