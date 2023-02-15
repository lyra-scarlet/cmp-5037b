import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;

public class VoiceSenderThread implements Runnable{

   static DatagramSocket sending_socket;

   public void start()
   {
      Thread thread = new Thread(this);
      System.out.println("Recording Audio...");
      thread.start();
   }

   public void run (){

      //***************************************************
      //Port to send to
      int PORT = 55555;
      //IP ADDRESS to send to
      InetAddress clientIP = null;
      try {
         clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
      } catch (UnknownHostException e) {
         System.out.println("ERROR: TextSender: Could not find client IP");
         e.printStackTrace();
         System.exit(0);
      }
      //***************************************************

      //***************************************************
      //Open a socket to send from
      //We dont need to know its port number as we never send anything to it.
      //We need the try and catch block to make sure no errors occur.

      //DatagramSocket sending_socket;
      try{
         sending_socket = new DatagramSocket();
      } catch (SocketException e){
         System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
         e.printStackTrace();
         System.exit(0);
      }
      //***************************************************

      //***************************************************
      //Main loop.

      AudioRecorder recorder = null;
      try
      {
         recorder = new AudioRecorder();
      }
      catch (LineUnavailableException e)
      {
         e.printStackTrace();
      }

      boolean running = true;
      int i = 0;
      while (running)
      {
         {
            try
            {
               byte[] block = recorder.getBlock();
               //Make a DatagramPacket from it, with client address and port number
               DatagramPacket packet = new DatagramPacket(block, block.length, clientIP, PORT);
               //Send it
               sending_socket.send(packet);
               //System.out.println("Sent packet");
               i++;
               System.out.println("Sending Packet " + i);

            }
            catch (IOException e)
            {
               System.out.println("ERROR: TextSender: Some random IO error occured!");
               e.printStackTrace();
            }
         }
      }
      //Close the socket
      sending_socket.close();
      //***************************************************
   }
}