import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Socket;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ClientServerIntegrationTest {

    private static Thread serverThread;
    private static int serverPort;
    private static Thread clientThread;

    @BeforeClass
    public void setUp() throws InterruptedException {
        // Запуск сервера в отдельном потоке
        serverPort = Settings.getServerPort();
        serverThread = new Thread(() -> {
            ServerRunner.main(new String[]{});
        });
        serverThread.start();

        // Даем серверу время на запуск
        Thread.sleep(1000);

        // Запуск клиента в отдельном потоке
        clientThread = new Thread(() -> {
            ClientRunner.main(new String[]{});
        });
        clientThread.start();

        // Даем клиенту время на подключение к серверу
        Thread.sleep(1000);
    }

    @Test
    public void testClientServerConnection() {
        // Проверяем, что клиент успешно подключился к серверу
        try (Socket socket = new Socket("localhost", serverPort)) {
            assertTrue(socket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Клиент не подключился к серверу");
        }
    }

    @AfterClass
    public void tearDown() {

        // Отключение всех клиентов
        Server.disconnectAllClients();

        // Остановка сервера
        Server.stopServer();
        try {
            serverThread.join(10000); // Ожидание до 10 секунд
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Остановка клиента
        clientThread.interrupt();
        try {
            clientThread.join(10000); // Ожидание до 10 секунд
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
