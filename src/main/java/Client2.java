import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class Client2 {
    private static final String SERVER_IP = Settings.getServerIp();
    private static final int SERVER_PORT = Settings.getServerPort();
    private static final File logFile = new File("fileClient2.log");

    public static void main(String[] args) throws IOException, InterruptedException {
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

        readerThread.join();

        socket.close();
        scanner.close();
    }

    private static Thread getThread(BufferedReader serverReader) {
        Thread readerThread = new Thread(() -> {
            try {
                String message;
                while ((message = serverReader.readLine()) != null) {
                    System.out.println(message);
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

    private static void log(String message) {
        try (FileWriter fileWriter = new FileWriter(logFile, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(new Date() + ": " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}