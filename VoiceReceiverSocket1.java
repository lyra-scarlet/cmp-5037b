import CMPC3M06.AudioPlayer;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class VoiceReceiverSocket1 implements Runnable
{
   static DatagramSocket receiving_socket;
   static AudioPlayer player;
   byte[] block = new byte[512];
   int sequence_num;

   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }

   public void run()
   {
      // Port to receive on
      int port = Config.getInt("port");
      // Open socket
      try { receiving_socket = new DatagramSocket(port); }
      catch (SocketException e)
      {
         System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
         e.printStackTrace();
         System.exit(0);
      }

      // Main loop
      try { player = new AudioPlayer(); }
      catch (LineUnavailableException e) { throw new RuntimeException(e); }
      System.out.println("Receiving audio...");

      while (true) try
      {
         // Receive a packet
         byte[] buffer = new byte[522];
         DatagramPacket packet = new DatagramPacket(buffer, 0, 522);
         receiving_socket.receive(packet);

         // Perform security checks
         block = Arrays.copyOfRange(buffer, 8, 520);
         if (Config.getBool("useDecryption")) SecurityLayer.EncryptDecrypt(block);
         if (Config.getBool("useChecksum"))
         {
            byte[] checksum = SecurityLayer.CalcChecksum(block);
            byte[] rcvChecksum = Arrays.copyOfRange(buffer, 520, 522);
            if (!Arrays.equals(checksum, rcvChecksum)) continue;
         }

         // Output sequence number
         byte[] byte_seq_num = Arrays.copyOfRange(buffer, 0, 8);
         sequence_num = ByteBuffer.wrap(byte_seq_num).getInt();
         System.out.println("Received Packet: " + sequence_num);

         // Play audio block
         player.playBlock(block);
      }
      // In the event of packet loss play nothing
      catch (IOException e)
      {
         if (e instanceof SocketTimeoutException) continue;
         System.out.println("ERROR: AudioReceiver: IO error occurred!");
         e.printStackTrace();
         break;
      }

      // Close the socket
      receiving_socket.close();
   }
}
