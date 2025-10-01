package com.esa.moviestar.libraries;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties props = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Error: cannot read " + CONFIG_FILE);
        }
    }

    public static String getApiKey() {
        return props.getProperty("API_KEY");
    }
}
