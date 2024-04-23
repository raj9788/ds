import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class ClockClient {

    // Client thread function used to send time at client side
    private static void startSendingTime(Socket slaveClient) {
        try {
            while (true) {
                // Provide server with clock time at the client
                OutputStream outputStream = slaveClient.getOutputStream();
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                outputStream.write(currentTime.getBytes());
                outputStream.flush();
                System.out.println("Recent time sent successfully time = "+currentTime+"\n");
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Client thread function used to receive synchronized time
    private static void startReceivingTime(Socket slaveClient) {
        try {
            while (true) {
                // Receive data from the server
                InputStream inputStream = slaveClient.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                String synchronizedTimeStr = new String(buffer, 0, bytesRead);
                Date synchronizedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(synchronizedTimeStr);

                System.out.println("Synchronized time at the client is: " + synchronizedTime + "\n");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // Function used to synchronize client process time
    private static void initiateSlaveClient(int port) {
        try {
            Socket slaveClient = new Socket("127.0.0.1", port);

            // Start sending time to server
            System.out.println("Starting to receive time from server\n");
            Thread sendTimeThread = new Thread(() -> startSendingTime(slaveClient));
            sendTimeThread.start();

            // Start receiving synchronized time from server
            System.out.println("Starting to receiving synchronized time from server\n");
            Thread receiveTimeThread = new Thread(() -> startReceivingTime(slaveClient));
            receiveTimeThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Driver function
    public static void main(String[] args) {
        // Initialize the Slave / Client
        initiateSlaveClient(14234);
    }
}



/*

                                                        After running server run client
                                                        using this      
-------------------------------------------------------- java ClockClient -------------------------------------
                                                        if multiple client required open new terminal and follow above steps/ commands
                                                        */
*/
