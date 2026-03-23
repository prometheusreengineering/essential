package studio.dreamys.prometheus.mixin.gg.essential.cosmetics;

import gg.essential.cosmetics.ExtensionsKt;
import gg.essential.gui.elementa.state.v2.ObservedInstant;
import gg.essential.network.cosmetics.Cosmetic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ExtensionsKt.class, remap = false)
public class MixinExtensionsKt {

    @Inject(method = "isAvailable", at = @At("HEAD"), cancellable = true)
    private static void onIsAvailable(Cosmetic cosmetic, ObservedInstant now, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
