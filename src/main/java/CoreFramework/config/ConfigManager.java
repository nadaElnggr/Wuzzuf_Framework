package CoreFramework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private static String get(String key) {
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isEmpty()) {
            return sysVal;
        }
        return props.getProperty(key);
    }

    public static String getBaseUrl() {
        return get("baseUrl");
    }

    public static String getBrowser() {
        return get("browser");
    }

    public static int getImplicitWait() {
        return Integer.parseInt(get("implicitWait"));
    }

    public static int getExplicitWait() {
        return Integer.parseInt(get("explicitWait"));
    }

    public static String getEnv() {
        return get("env");
    }

    public static boolean isProd() {
        String env = getEnv();
        return env != null && env.equalsIgnoreCase("prod");
    }

    public static String getSmtpUser() {
        return get("smtpUser");
    }

    public static String getSmtpPassword() {
        return get("smtpPassword");
    }

    public static String[] getNotificationEmails() {
        String raw = get("notificationEmails");
        if (raw == null || raw.isBlank()) {
            return new String[0];
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

}
