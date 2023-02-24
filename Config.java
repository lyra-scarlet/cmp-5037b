/*
 * Config.java
 */

import java.io.*;
import java.util.Properties;

public class Config {
    private static FileInputStream stream;
    private static Properties prop = new Properties();
    static {
        try {
            stream = new FileInputStream("app.config");
            prop.load(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties get() {
        return prop;
    }

    public static String getString(String property) {
        return prop.getProperty(property);
    }

    public static int getInt(String property) {
        return Integer.parseInt(prop.getProperty(property));
    }

    public static long getLong(String property) {
        return Long.parseLong(prop.getProperty(property));
    }
}
