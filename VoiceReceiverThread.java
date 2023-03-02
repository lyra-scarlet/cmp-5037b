import uk.ac.uea.cmp.voip.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class VoiceReceiverThread extends Thread
{
   static DatagramSocket receiving_socket;
   static byte[] buffer = new byte[520];
   static Queue<byte[]> sharedQueue = new LinkedList<>();


   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }

   public void run()
   {
      PlayerThread playerThread = new PlayerThread(sharedQueue);
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
      System.out.println("Receiving audio...");
      int sequenceNum;

      synchronized (sharedQueue)
      {
         while (sharedQueue.isEmpty()) try
         {
            DatagramPacket packet = new DatagramPacket(buffer, 0, 520);

            receiving_socket.setSoTimeout(100);
            receiving_socket.receive(packet);

            byte[] byte_seq_num = Arrays.copyOfRange(buffer, 0, 8);
            sequenceNum = ByteBuffer.wrap(byte_seq_num).getInt();
            System.out.println("Received Packet: " + sequenceNum);

            byte[] block = Arrays.copyOfRange(buffer, 8, 520);
            sharedQueue.add(block);

         } catch (IOException e) {
         if (e instanceof SocketTimeoutException) {
            // TELL PLAYER THREAD TO REPEAT LAST SUCCESSFUL BLOCK.
            byte[] block = Arrays.copyOfRange(buffer, 8, 520);
            sharedQueue.add(block);
            sharedQueue.notify();
            continue;
         }
         System.out.println("ERROR: AudioReceiver: IO error occurred!");
         e.printStackTrace();
         break;
         }
      }
//      while (true) try {
//         // Receive a DatagramPacket
//         // **********************************************************************************
//         DatagramPacket packet = new DatagramPacket(buffer, 0, 520);
//
//         receiving_socket.setSoTimeout(32);
//         receiving_socket.receive(packet);
//
//         // Play data from the byte buffer
//         byte[] byte_seq_num = Arrays.copyOfRange(buffer, 0, 8);
//         sequenceNum = ByteBuffer.wrap(byte_seq_num).getInt();
//         byte[] block = Arrays.copyOfRange(buffer, 8, 520);
//         blockQueue.add(block);
//         blockQueue.remove(block);
//         System.out.println("Received Packet: " + sequenceNum);
//
//         // **********************************************************************************
//      } catch (IOException e) {
//         if (e instanceof SocketTimeoutException) {
//            continue;
//         }
//         System.out.println("ERROR: AudioReceiver: IO error occurred!");
//         e.printStackTrace();
//         break;
//      }

      // Close the socket
      receiving_socket.close();
   }
}