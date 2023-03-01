import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.*;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class VoiceReceiverThread implements Runnable
{
   static DatagramSocket receiving_socket;
   static AudioPlayer player;
   static byte[] block = new byte[512];

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
      int socket = Config.getInt("socket");
      try {
         switch (socket) {
            case 1 -> receiving_socket = new DatagramSocket(port);
            case 2 -> receiving_socket = new DatagramSocket2(port);
            case 3 -> receiving_socket = new DatagramSocket3(port);
            case 4 -> receiving_socket = new DatagramSocket4(port);
            default -> throw new SocketException(socket + " is not a valid socket number");
         }
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
      int sequence_num;
      while (true) try {
         // Receive a DatagramPacket
         // **********************************************************************************
         byte[] buffer = new byte[520];
         DatagramPacket packet = new DatagramPacket(buffer, 0, 520);

         receiving_socket.setSoTimeout(32);
         receiving_socket.receive(packet);

         // Play data from the byte buffer
         byte[] byte_seq_num = Arrays.copyOfRange(buffer, 0, 8);
         byte[] block = Arrays.copyOfRange(buffer, 8, 520);
         sequence_num = ByteBuffer.wrap(byte_seq_num).getInt();
         player.playBlock(block);
         System.out.println("Received Packet: " + sequence_num);

         // **********************************************************************************
      } catch (IOException e) {
         if (e instanceof SocketTimeoutException) {
            try
            {
               player.playBlock(block);
            }
            catch (IOException ex)
            {
               ex.printStackTrace();
            }
            for (int j = 0; j < block.length; j++)
               block[j] *= 0.8;
            continue;
         }
         System.out.println("ERROR: AudioReceiver: IO error occurred!");
         e.printStackTrace();
         break;
      }

      // Close the socket
      receiving_socket.close();
   }
}