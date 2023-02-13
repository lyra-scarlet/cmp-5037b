/*
 * TextReceiver.java
*/

/**
 *
 * @author  abj
 */
import CMPC3M06.AudioPlayer;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Vector;

public class AudioReceiver {

    static DatagramSocket receiving_socket;

    public static void main (String[] args) throws Exception{

        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************

        //***************************************************
        //Open a socket to receive from on port PORT

        //DatagramSocket receiving_socket;
        try{
		receiving_socket = new DatagramSocket(PORT);
	} catch (SocketException e){
                System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
		e.printStackTrace();
                System.exit(0);
	}
        //***************************************************

        //***************************************************
        //Main loop.

        boolean running = true;
        AudioPlayer player = new AudioPlayer();

        while (running){

            try{
                //Receive a DatagramPacket
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

                receiving_socket.receive(packet);

                //Get data from the byte buffer
                player.playBlock(buffer);
                System.out.println("Packet received");

            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occurred!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
    }
}
