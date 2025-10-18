package studio.dreamys.prometheus;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Prometheus implements PreLaunchEntrypoint {
    private static final Logger logger = LogManager.getLogger("Prometheus");

    @Override
    public void onPreLaunch() {
        MixinBootstrap.init();
        Mixins.addConfiguration("prometheus.mixins.json");
        logger.debug("getUnvisitedCount() = {}", Mixins.getUnvisitedCount());

        try {
            chainLoadMixins();
        } catch (ReflectiveOperationException e) {
            logger.error("Failed to chain load mixins", e);
        }
    }

    //https://github.com/EssentialGG/EssentialLoader/blob/master/stage2/fabric/src/main/java/gg/essential/loader/stage2/EssentialLoader.java#L180
    public static void chainLoadMixins() throws ReflectiveOperationException {
        if (Mixins.getUnvisitedCount() != 0) {
            MixinEnvironment environment = MixinEnvironment.getDefaultEnvironment();
            Object transformer = environment.getActiveTransformer();

            Field processorField = transformer.getClass().getDeclaredField("processor");
            processorField.setAccessible(true);

            Object processor = processorField.get(transformer);

            Method select = processor.getClass().getDeclaredMethod("select", MixinEnvironment.class);
            select.setAccessible(true);
            select.invoke(processor, environment);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
