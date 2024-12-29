package studio.dreamys.prometheus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Prometheus implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(Prometheus.class);
    FilenameFilter isJar = (dir, name) -> name.endsWith(".jar");
    @Override
    public void onInitializeClient() {
        LOGGER.debug("Prometheus is initializing");
        if (!FabricLoader.getInstance().isModLoaded("essential-container") && FabricLoader.getInstance().isModLoaded("essential")) {
            LOGGER.warn("Essential is already loaded, skipping symlink creation");
            return;
        }
        File modsDir = new File("mods");
        assert modsDir.exists();
        Path essentialDir = modsDir.toPath().toAbsolutePath().getParent().resolve("essential");
        LOGGER.debug("Essential Directory: {}", essentialDir.toAbsolutePath());
        Path symlinkJarPath = modsDir.toPath();
        // Create symlink
        if (!essentialDir.toFile().exists()) {
            return; // Essential not found, let it create itself
        }
        for (File file : Objects.requireNonNull(essentialDir.toFile().listFiles(isJar))) {
            try {
                Path symlinkJar = symlinkJarPath.resolve(file.getName());
                if (symlinkJar.toFile().exists()) {
                    LOGGER.warn("Symlink already exists: {}", symlinkJar.toAbsolutePath());
                    break;
                }
                Files.createSymbolicLink(symlinkJar, file.toPath());
                LOGGER.info("Created symlink: {}", symlinkJar.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}