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
   static PlayerThread playerThread = new PlayerThread();
   static byte[] buffer = new byte[520];

   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }

   public void run()
   {
      playerThread.start();
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
      long sequence_num;
      while (true) try {
         // Receive a DatagramPacket
         // **********************************************************************************
         DatagramPacket packet = new DatagramPacket(buffer, 0, 524);

         receiving_socket.setSoTimeout(32);
         receiving_socket.receive(packet);

         // Get data from the byte buffer
         byte[] byte_seq_num = Arrays.copyOfRange(buffer, 0, 8);
         byte[] new_block = Arrays.copyOfRange(buffer, 8, 520);
         byte[] checksum = Arrays.copyOfRange(buffer, 520, 524);
         // Decrypt audio block
         SecurityLayer.EncryptDecrypt(new_block);
         // Check checksum
         byte[] checksum_data = ByteBuffer.wrap(new byte[520]).put(byte_seq_num).put(new_block).array();
         if (!Arrays.equals(checksum, SecurityLayer.CalcChecksum(checksum_data)))
            throw new SocketTimeoutException("Checksum mismatch");
         // Play block
         block = new_block;
         sequence_num = ByteBuffer.wrap(byte_seq_num).getLong();
         System.out.println("Received Packet: " + sequence_num);
//         long start = System.currentTimeMillis();
         player.playBlock(block);
         playerThread.setBlock(block);
//         long finish = System.currentTimeMillis();
//         long timeElapsed = finish - start;
//         System.out.println("timeElapsed in milliseconds (new block): " + timeElapsed);


         // **********************************************************************************
      } catch (IOException e) {
         if (e instanceof SocketTimeoutException) {
            playerThread.setBlock(block);
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