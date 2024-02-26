import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {
    private static final String SETTINGS_FILE = "settings.txt";

    public static int getServerPort() {
        Properties properties = new Properties();
        if (Files.exists(Path.of(SETTINGS_FILE))) {
            try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            try {
                properties.load(Settings.class.getClassLoader().getResourceAsStream(SETTINGS_FILE));
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }

        return Integer.parseInt(properties.getProperty("PORT"));
    }

    public static String getServerIp() {
        Properties properties = new Properties();

        if (Files.exists(Path.of(SETTINGS_FILE))) {
            try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try (InputStream resourceStream = Settings.class.getClassLoader().getResourceAsStream(SETTINGS_FILE)) {
                if (resourceStream != null) {
                    properties.load(resourceStream);
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


        return properties.getProperty("IP");
    }
}