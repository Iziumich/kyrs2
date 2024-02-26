import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private final int port;
    private static ServerSocket serverSocket;
    private static boolean isRunning = false;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private final File logFile = new File("serverLog.log");

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен на порту " + port);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Новое подключение: " + clientSocket);

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientHandler.start();
                    clients.add(clientHandler);
                } catch (SocketException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        isRunning = false;
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

    public void broadcast(String message, ClientHandler excludeClient) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != excludeClient) {
                    client.sendMessage(message);
                }
            }
        }
        log(message);
    }

    private void log(String message) {
        try (FileWriter fileWriter = new FileWriter(logFile, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private final Server server;
        private final Socket clientSocket;
        private PrintWriter writer;
        private BufferedReader reader;

        public ClientHandler(Socket socket, Server server) {
            this.clientSocket = socket;
            this.server = server;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String username = reader.readLine();
                if (username == null || username.trim().isEmpty()) {
                    writer.println("Имя пользователя не может быть пустым. Пожалуйста, попробуйте еще раз.");
                    server.log("Клиент отключился с пустым именем пользователя");
                    clientSocket.close();
                    return;
                }

                server.broadcast(username + " присоединился к чату", this);
                server.log(username + " присоединился к чату");

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equals("/exit")) {
                        server.broadcast(username + " покинул чат", this);
                        server.log(username + " покинул чат");
                        clientSocket.close();
                        break;
                    }
                    server.broadcast(username + ": " + message, this);
                    server.log(username + ": " + message);
                }
            } catch (SocketException e) {
                server.log("Клиент отключился: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnectClient();
            }
        }

        public void disconnectClient() {
            clients.remove(this);
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

        public void sendMessage(String message) {
            writer.println(message);
        }
    }
}