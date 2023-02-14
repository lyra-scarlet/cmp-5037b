/*
 * DatagramTester.java
 */

import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.HexFormat;
import uk.ac.uea.cmp.voip.*;

public class DatagramTester {
    static DatagramSocket receiving_socket;
    static DatagramSocket sending_socket;
    static InetAddress clientIP;

    private static void setClientIP() {
        // IP address to send to
        try {
            clientIP = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.out.println("Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void setSockets(int port, int socket) {
        // Open sockets
        try {
            switch (socket) {
                case 1:     sending_socket = new DatagramSocket();
                            receiving_socket = new DatagramSocket(port);
                            break;
                case 2:     sending_socket = new DatagramSocket2();
                            receiving_socket = new DatagramSocket2(port);
                            break;
                case 3:     sending_socket = new DatagramSocket3();
                            receiving_socket = new DatagramSocket3(port);
                            break;
                case 4:     sending_socket = new DatagramSocket4();
                            receiving_socket = new DatagramSocket4(port);
                            break;
                default:    System.out.println(socket+" is not a valid socket number");
                            System.exit(0);

            }
        } catch (SocketException e) {
            System.out.println("Couldn't open sockets!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String args[]) throws Exception {
        // Testing definitions
        int timeout = 50; // ms to wait for timeout
        int packets = 500; // amount of packets to send
        int socket = 3; // number of socket to test (1 to 4)

        // Get config
        Properties prop = Config.get();
        int port = Integer.parseInt(prop.getProperty("port"));
        // Set static variables and also set packet timeout
        DatagramTester.setClientIP();
        DatagramTester.setSockets(port,socket);
        receiving_socket.setSoTimeout(timeout);
        double loss = 0;
        double accuracy = 0;
        // Main loop
        for (int i = 0; i < packets; i++) {
            // Make 512 bytes of random data.
            byte[] data = new byte[512];
            new Random().nextBytes(data);

            // Send and receive packet
            byte[] sendBuffer = Arrays.copyOf(data, data.length);
            byte[] receiveBuffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(sendBuffer, 512, clientIP, port);
            sending_socket.send(packet);
            packet = new DatagramPacket(receiveBuffer, 0, 512);
            packet.setData(receiveBuffer, 0, 512);
            try {
                receiving_socket.receive(packet);
                // Compare packet
                double correctness = 0;
                // Calculate correctness, using for loop to compare send and receive buffers
                for (int j = 0; j < 512; j++) if (data[j] == receiveBuffer[j]) correctness++;
                correctness = correctness / 512 * 100;
                System.out.println("Packet "+i+" has a correctness of "+correctness+"%");
                accuracy += correctness;
            } catch (SocketTimeoutException e) {
                loss++;
                System.out.println("Packet "+i+" not received with a timeout of "+timeout+"ms");
            }
        }
        // Calculate packet loss and accuracy
        System.out.println("\nSent "+packets+" packets and lost "+(int)loss);
        double received = 100 - (loss / packets * 100);
        System.out.println("Percentage of packets received: "+received+"%");
        accuracy = accuracy / (packets - loss);
        System.out.println("Accuracy of received packets: "+accuracy+"%");
    }
}