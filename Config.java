/*
 * Config.java
 */

import java.io.*;
import java.util.Properties;

public class Config {
    public static Properties get() {
        Properties prop = new Properties();
        String fileName = "app.config";
        try (FileInputStream stream = new FileInputStream(fileName)) {
            prop.load(stream);
        } catch (Exception ex) {
            // Caught, but unhandled - might be worth handling later
        }
        return prop;
    }
}
