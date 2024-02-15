import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    static int port = Settings.getServerPort();
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final File logFile = new File("serverLog.log");
    private static ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            } catch (SocketException e) {
                break;
            }
        }
    }

    public static void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectAllClients() {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.disconnectClient();
            }
            clients.clear();
        }
    }

    static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter writer;
        private BufferedReader reader;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String username = reader.readLine();
                if (username == null || username.trim().isEmpty()) {
                    writer.println("Username cannot be empty. Please try again.");
                    log("Client disconnected with empty username");
                    clientSocket.close();
                    return;
                }

                synchronized (clients) {
                    clients.add(this);
                }

                broadcast(username + " joined the chat", this);
                log(username + " joined the chat");

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equals("/exit")) {
                        clients.remove(this);
                        broadcast(username + " left the chat", this);
                        log(username + " left the chat");
                        clientSocket.close();
                        break;
                    }
                    broadcast(username + ": " + message, this);
                    log(username + ": " + message);
                }
            } catch (SocketException e) {
                log("Client disconnected: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void disconnectClient() {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clients) {
                    clients.remove(this);
                }
            }
        }

        private void broadcast(String message, ClientHandler excludeClient) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != excludeClient) {
                        client.writer.println(message);
                    }
                }
            }
            log(message);
        }

        private void log(String message) {
            try (FileWriter fileWriter = new FileWriter(logFile, true);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {
                printWriter.println(new Date() + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}