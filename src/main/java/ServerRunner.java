public class ServerRunner {
    public static void main(String[] args) {
        Server server = new Server(Settings.getServerPort());
        server.start();
    }
}
