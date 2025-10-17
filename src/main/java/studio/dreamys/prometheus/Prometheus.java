package studio.dreamys.prometheus;

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

public class Prometheus implements PrePrePreLaunchEntrypoint {
    Logger logger = LogManager.getLogger("Prometheus");

    @Override
    public void onLanguageAdapterLaunch() {
        if (FabricLoader.getInstance().getModContainer("essential").isPresent()) return;
        // Get the name of the essential stage2 jar
        AtomicReference<String> mcVersion = new AtomicReference<>("unknown");
        logger.debug("Getting minecraft version!");
        FabricLoader.getInstance().getModContainer("minecraft").ifPresent(modContainer -> {
            mcVersion.set(modContainer.getMetadata().getVersion().getFriendlyString());
        });
        String jarName = "Essential (fabric_" + mcVersion.get() + ").jar";
        logger.info("Loading essential jar: {}", jarName);
        File essentialJar = new File(FabricLoader.getInstance().getGameDir().toFile(), "essential/" + jarName);
        if (!essentialJar.exists()) {
            logger.warn("Essential jar not found! Expected at: {}", essentialJar.getAbsolutePath());
            return;
        }
        try {
            URI u = essentialJar.toURI();
            Class<?> knotClassLoaderClass = Prometheus.class.getClassLoader().getClass();
            for(Method m : knotClassLoaderClass.getMethods()) {
                logger.info("m.getName() = {}", m.getName());
            }
            Method method = knotClassLoaderClass.getDeclaredMethod("addUrlFwd", URL.class);
            method.setAccessible(true);
            method.invoke(this.getClass().getClassLoader(), u.toURL());
        } catch (Exception e) {
            logger.error("Failed to load essential jar to classpath!", e);
        }
    }
}
