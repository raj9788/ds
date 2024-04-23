// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.text.ParseException;
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.TimeUnit;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClockServer {

    // Data structure used to store client address and clock data
    private static final Map<String, Map<String, Object>> clientData = new HashMap<>();

    // Thread function used to receive clock time from a connected client
    private static void startReceivingClockTime(Socket connector, String address) {
        try {
            while (true) {
                // Receive clock time
                InputStream inputStream = connector.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                String clockTimeString = new String(buffer, 0, bytesRead);
                Date clockTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(clockTimeString);
                long clockTimeDiff = System.currentTimeMillis() - clockTime.getTime();

                Map<String, Object> data = new HashMap<>();
                data.put("clock_time", clockTime);
                data.put("time_difference", clockTimeDiff);
                data.put("connector", connector);

                clientData.put(address, data);

                System.out.println("Client Data updated with: " + address + "\n");

                TimeUnit.SECONDS.sleep(5);
            }
        } catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Master thread function used to open portal for accepting clients over given port
    private static void startConnecting(ServerSocket masterServer) {
        try {
            while (true) {
                // Accepting a client
                Socket masterSlaveConnector = masterServer.accept();
                String slaveAddress = masterSlaveConnector.getInetAddress().getHostAddress() + ":" + masterSlaveConnector.getPort();

                System.out.println(slaveAddress + " got connected successfully\n");

                Thread currentThread = new Thread(() -> startReceivingClockTime(masterSlaveConnector, slaveAddress));
                currentThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Subroutine function used to fetch average clock difference
    private static long getAverageClockDiff() {
        long sumOfClockDifference = 0;

        for (Map<String, Object> client : clientData.values()) {
            sumOfClockDifference += (long) client.get("time_difference");
        }

        return sumOfClockDifference / clientData.size();
    }

    // Master sync thread function used to generate cycles of clock synchronization in the network
    private static void synchronizeAllClocks() {
        try {
            while (true) {
                System.out.println("New synchronization cycle started.");
                System.out.println("Number of clients to be synchronized: " + clientData.size() + "\n");

                if (!clientData.isEmpty()) {
                    long averageClockDifference = getAverageClockDiff();

                    for (Map.Entry<String, Map<String, Object>> entry : clientData.entrySet()) {
                        try {
                            long synchronizedTime = System.currentTimeMillis() + averageClockDifference;
                            Socket connector = (Socket) entry.getValue().get("connector");
                            OutputStream outputStream = connector.getOutputStream();
                            outputStream.write(String.valueOf(synchronizedTime).getBytes());
                            outputStream.flush();
                        } catch (IOException e) {
                            System.out.println("Something went wrong while sending synchronized time through " + entry.getKey());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("No client data. Synchronization not applicable.\n");
                }

                TimeUnit.SECONDS.sleep(5);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Function used to initiate the Clock Server / Master Node
    private static void initiateClockServer(int port) {
        try {
            ServerSocket masterServer = new ServerSocket(port);
            System.out.println("Socket at master node created successfully\n");

            System.out.println("Clock server started...\n");

            // Start making connections
            System.out.println("Starting to make connections...\n");
            Thread masterThread = new Thread(() -> startConnecting(masterServer));
            masterThread.start();

            // Start synchronization
            System.out.println("Starting synchronization parallelly...\n");
            Thread syncThread = new Thread(ClockServer::synchronizeAllClocks);
            syncThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Driver function
    public static void main(String[] args) {
        // Trigger the Clock Server
        initiateClockServer(14234);
    }
}


/*

                                                FIRST RUN SERVER 
                                                USING TTHIS
--------------------------------------------   javac *.java --------------------------------------------- compile all program
-------------------------------------------- java ClockServer ----------------------------------------------- 

