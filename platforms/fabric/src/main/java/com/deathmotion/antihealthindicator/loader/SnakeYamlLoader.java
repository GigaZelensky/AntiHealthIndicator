package com.deathmotion.antihealthindicator.loader;

import com.deathmotion.antihealthindicator.AHIFabric;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SnakeYamlLoader {
    private static final Logger LOGGER = LogManager.getLogger(SnakeYamlLoader.class);

    private static final String SNAKEYAML_VERSION = "2.2";
    private static final String SNAKEYAML_JAR_NAME = "snakeyaml-" + SNAKEYAML_VERSION + ".jar";
    private static final Path SNAKEYAML_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(AHIFabric.MOD_ID)
            .resolve("libraries")
            .resolve(SNAKEYAML_JAR_NAME);
    private static final String SNAKEYAML_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/yaml/snakeyaml/" + SNAKEYAML_VERSION + "/" + SNAKEYAML_JAR_NAME;

    public static void loadSnakeYaml() {
        // Check if the SnakeYAML jar exists; download if it doesn't
        if (!Files.exists(SNAKEYAML_PATH)) {
            downloadSnakeYaml();
        } else {
            LOGGER.info("SnakeYAML is already present at " + SNAKEYAML_PATH);
        }

        // Attempt to add SnakeYAML to the classpath
        try {
            addJarToClasspath();
        } catch (Exception e) {
            LOGGER.error("Failed to load SnakeYAML dynamically: " + e.getMessage(), e);
        }
    }

    private static void downloadSnakeYaml() {
        try {
            Files.createDirectories(SNAKEYAML_PATH.getParent());
            URL url = new URL(SNAKEYAML_DOWNLOAD_URL);
            Files.copy(url.openStream(), SNAKEYAML_PATH, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Downloaded SnakeYAML to " + SNAKEYAML_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to download SnakeYAML: " + e.getMessage(), e);
        }
    }

    private static void addJarToClasspath() throws Exception {
        // Get the class loader and use reflection to add the jar URL to its classpath
        ClassLoader classLoader = FabricLoader.getInstance().getClass().getClassLoader();
        URL jarUrl = SnakeYamlLoader.SNAKEYAML_PATH.toUri().toURL();
        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addUrlMethod.setAccessible(true);
        addUrlMethod.invoke(classLoader, jarUrl);
        LOGGER.info("Loaded SnakeYAML dynamically from: " + SnakeYamlLoader.SNAKEYAML_PATH);
    }
}
