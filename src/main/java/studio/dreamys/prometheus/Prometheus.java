package studio.dreamys.prometheus;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class Prometheus implements PreLaunchEntrypoint {
    private static final Logger logger = Logger.getLogger("Prometheus");

    @Override
    public void onPreLaunch() {
        MixinBootstrap.init();
        Mixins.addConfiguration("prometheus.mixins.json");
        logger.fine(String.format("getUnvisitedCount() = %s", Mixins.getUnvisitedCount()));

        try {
            chainLoadMixins();
        } catch (ReflectiveOperationException e) {
            logger.severe(String.format("Failed to chain load mixins\n%s", e));
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
}
