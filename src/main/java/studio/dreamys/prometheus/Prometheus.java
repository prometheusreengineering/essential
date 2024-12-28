package studio.dreamys.prometheus;

import net.fabricmc.api.ClientModInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Prometheus implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Test from prometheus");
        try {
            chainLoadMixins();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();

        }
    }
    private void chainLoadMixins() throws ReflectiveOperationException {
        if (Mixins.getUnvisitedCount() == 0) {
            return; // nothing to do, Mixin already loaded our config by itself
        }

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