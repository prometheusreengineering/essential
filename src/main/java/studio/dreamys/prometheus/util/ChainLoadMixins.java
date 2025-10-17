package studio.dreamys.prometheus.util;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ChainLoadMixins {
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
