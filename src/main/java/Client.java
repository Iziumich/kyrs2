import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = Settings.getServerIp();
    private static final int SERVER_PORT = Settings.getServerPort();
    private static final File logFile = new File("fileClient.log");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void start() throws IOException {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        log("Connected to the chat server");

        BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);

        Scanner scanner = new Scanner(System.in);
        String username = "";
        while (username.trim().isEmpty()) {
            System.out.print("Enter your username: ");
            username = scanner.nextLine();
            if (username.trim().isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
            }
        }
        serverWriter.println(username);
        log("Username sent: " + username);

        Thread readerThread = getThread(serverReader);

        while (true) {
            String message = scanner.nextLine();
            serverWriter.println(message);
            log("Sent message: " + message);
            if (message.equals("/exit")) {
                break;
            }
        }

        try {
            readerThread.join();
            socket.close();
            scanner.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Thread getThread(BufferedReader serverReader) {
        Thread readerThread = new Thread(() -> {
            try {
                String message;
                while ((message = serverReader.readLine()) != null) {
                    printMessageWithTimestamp(message);
                    log("Message received: " + message);
                }
            } catch (IOException e) {
                log("Error when reading from server.");
                e.printStackTrace();
            } finally {
                try {
                    serverReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.start();
        return readerThread;
    }

    private void printMessageWithTimestamp(String message) {
        String timestamp = dateFormat.format(new Date());
        System.out.println(timestamp + ": " + message);
    }

    private void log(String message) {
        try (FileWriter fileWriter = new FileWriter(logFile, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            String timestamp = dateFormat.format(new Date());
            printWriter.println(timestamp + ": " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}