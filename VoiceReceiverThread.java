import uk.ac.uea.cmp.voip.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
public class VoiceReceiverThread implements Runnable
{
   static DatagramSocket receiving_socket;
   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }
   public void run()
   {
      PlayerThread playerThread = new PlayerThread();
      playerThread.start();

      int port = Config.getInt("port");
      int socket = Config.getInt("socket");
      int sequence_num;

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
      System.out.println("Receiving audio...");

      while (true) try {
         byte[] buffer = new byte[520];
         DatagramPacket packet = new DatagramPacket(buffer, 0, 520);

         receiving_socket.setSoTimeout(32);
         receiving_socket.receive(packet);

         byte[] byte_seq_num = Arrays.copyOfRange(buffer, 0, 8);
         byte[] block = Arrays.copyOfRange(buffer, 8, 520);

         sequence_num = ByteBuffer.wrap(byte_seq_num).getInt();
         playerThread.addToQueue(block);
         System.out.println("Received Packet: " + sequence_num);
      } catch (IOException e) {
         if (e instanceof SocketTimeoutException) {
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