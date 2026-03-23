package studio.dreamys.prometheus.mixin.gg.essential.network.connectionmanager.cosmetics;

import gg.essential.event.client.ClientTickEvent;
import gg.essential.gui.elementa.state.v2.MutableState;
import gg.essential.network.connectionmanager.cosmetics.CosmeticsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CosmeticsManager.class, remap = false)
public abstract class MixinCosmeticsManager {

    @Shadow
    public abstract void unlockAllCosmetics();

    @Shadow
    private MutableState<Boolean> cosmeticsLoaded;

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(ClientTickEvent tickEvent, CallbackInfo ci) {
        if (cosmeticsLoaded.getUntracked())
            unlockAllCosmetics();
    }
}
