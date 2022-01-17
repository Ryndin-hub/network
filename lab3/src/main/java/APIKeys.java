import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIKeys {
    public static String[] getKeys() throws IOException {
        InputStream fis = new FileInputStream("src\\main\\resources\\keys.properties");
        Properties properties = new Properties();
        properties.load(fis);
        String[] keys = new String[3];
        keys[0] = properties.getProperty("GRAPHHOPPER_KEY");
        keys[1] = properties.getProperty("OPENTRIPMAP_KEY");
        keys[2] = properties.getProperty("OPENWEATHERMAP_KEY");
        for (int i = 0; i < 3; i++) {
            if (keys[i].equals("<INSERT_YOUR_KEY>")) {
                System.out.println("Insert your keys in keys.properties file");
                return null;
            }
        }
        return keys;
    }
}
