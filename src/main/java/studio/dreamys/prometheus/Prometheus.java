package studio.dreamys.prometheus;

import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.dreamys.prometheus.util.LoaderInternals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class Prometheus implements PrePrePreLaunchEntrypoint, PreLaunchEntrypoint {
    private static final Logger logger = LogManager.getLogger("Prometheus");

    LoaderInternals loaderInternals = new LoaderInternals(this.getClass().getClassLoader(), logger);

    @Override
    public void onLanguageAdapterLaunch() {
        // Load everything here so we beat mixins
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
        URI u = essentialJar.toURI();

        try {
            loaderInternals.addToClassLoader(u.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        // TODO: use {@link LoaderInternals} to apply our patches
        /*
        try {
            ClassLoaders.addToSystemClassPath(u.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Class.forName("gg.essential.loader.stage2.EssentialLoader", true, classLoader).getConstructor(Path.class, String.class).newInstance(gameDir, this.gameVersion).getClass().getMethod("load").invoke(this.stage2);
        /*
        URLClassLoader urlClassLoader = null;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{u.toURL()}, this.getClass().getClassLoader());
            urlClassLoader.loadClass("gg.essential.network.connectionmanager.cosmetics.CosmeticsManager");
            urlClassLoader.loadClass("gg.essential.network.connectionmanager.handler.cosmetics.ServerCosmeticsPopulatePacketHandler");
        } catch (Exception e) {
            logger.error("Failed to load essential jar to classpath!", e);
        }
        /**///
    }

    @Override
    public void onPreLaunch() {
        // we are currently blocking essential from loading its stage0, maybe load their updater here?
    }
}
