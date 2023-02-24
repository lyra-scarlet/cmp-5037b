/*
 * DatagramTester.java
 */

import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
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
                case 1 -> {
                    sending_socket = new DatagramSocket();
                    receiving_socket = new DatagramSocket(port);
                } case 2 -> {
                    sending_socket = new DatagramSocket2();
                    receiving_socket = new DatagramSocket2(port);
                } case 3 -> {
                    sending_socket = new DatagramSocket3();
                    receiving_socket = new DatagramSocket3(port);
                } case 4 -> {
                    sending_socket = new DatagramSocket4();
                    receiving_socket = new DatagramSocket4(port);
                } default -> {
                    System.out.println(socket + " is not a valid socket number");
                    System.exit(0);
                }
            }
        } catch (SocketException e) {
            System.out.println("Couldn't open sockets!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {
        // Get config
        Properties prop = Config.get();
        // Testing definitions
        int timeout = Integer.parseInt(prop.getProperty("timeout"));
        int socket = Integer.parseInt(prop.getProperty("socket"));
        int packets = 500; // amount of packets to send
        int port = Integer.parseInt(prop.getProperty("port"));
        // Set static variables and also set packet timeout
        DatagramTester.setClientIP();
        DatagramTester.setSockets(port,socket);
        receiving_socket.setSoTimeout(timeout);
        double loss = 0;
        double accuracy = 0;
        double burstGap = 1;
        double burstLength = 0;
        double avgBurstLength = 0;
        double avgBurstGap = 0;
        int bursts = 0;
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
                // Calculate correctness, using for loop to compare sent and received data
                double correctness = 0;
                for (int j = 0; j < 512; j++) if (data[j] == receiveBuffer[j]) correctness++;
                correctness = correctness / 512 * 100;
                System.out.println("Packet "+i+" has a correctness of "+correctness+"%");
                if (correctness >= 100) accuracy++;
                if (burstLength > 0) {
                    avgBurstLength *= bursts-1;
                    avgBurstLength = (avgBurstLength+burstLength)/bursts;
                    burstLength = 0;
                }
                burstGap++;
            } catch (SocketTimeoutException e) {
                loss++;
                System.out.println("Packet "+i+" not received with a timeout of "+timeout+"ms");
                if (burstGap > 0) {
                    avgBurstGap *= bursts;
                    bursts++;
                    avgBurstGap = (avgBurstGap+burstGap)/bursts;
                    burstGap = 0;
                }
                burstLength++;
            }
        }
        // Calculate packet loss and accuracy
        System.out.println("\nSent "+packets+" packets and lost "+(int)loss);
        accuracy = accuracy / packets * 100;
        System.out.println("Percentage of correct packets: "+accuracy+"%");
        loss = loss / packets * 100;
        System.out.println("Percentage of packets lost: "+loss+"%");
        if (bursts > 0) {
            System.out.println("Number of bursts: "+bursts);
            System.out.println("Average burst length: " + avgBurstLength);
            System.out.println("Average burst gap: " + avgBurstGap);
        }
    }
}