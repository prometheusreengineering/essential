package studio.dreamys.prometheus.mixin.gg.essential.network.connectionmanager.cosmetics;

import gg.essential.event.client.ClientTickEvent;
import gg.essential.event.network.server.ServerJoinEvent;
import gg.essential.network.connectionmanager.ConnectionManager;
import gg.essential.network.connectionmanager.cosmetics.CosmeticsManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CosmeticsManager.class, remap = false)
public abstract class MixinCosmeticsManager {
    @Shadow
    @Final
    private @NotNull ConnectionManager connectionManager;

    @Shadow
    public abstract void unlockAllCosmetics();

//    @Redirect(method = "onWorldJoin", at = @At(value = "INVOKE", target = "Lgg/essential/network/connectionmanager/ConnectionManager;isAuthenticated()Z"))
//    public boolean isAuthenticated(ConnectionManager instance) {
//        return true;
//    }
//
//    @Redirect(method = "onWorldJoin", at = @At(value = "INVOKE", target = "Lgg/essential/config/EssentialConfig;getDisableCosmetics()Z"))
//    public boolean getDisableCosmetics(EssentialConfig instance) {
//        return true;
//    }
//
//    @Inject(method = "onWorldJoin", at = @At(value = "INVOKE", target = "Lgg/essential/network/connectionmanager/cosmetics/ConnectionManagerKt;unlockServerCosmetics(Lgg/essential/network/connectionmanager/ConnectionManager;Ljava/lang/String;)V"))
//    public void onWorldJoin(ServerJoinEvent event, CallbackInfo ci) {
//        ConnectionManagerKt.unlockSpsCosmetics(connectionManager);
//    }

    @Overwrite
    public void onWorldJoin(ServerJoinEvent event) {

    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lgg/essential/gui/elementa/state/v2/MutableState;set(Ljava/lang/Object;)V"))
    public void onTick(ClientTickEvent tickEvent, CallbackInfo ci) {
        unlockAllCosmetics();
    }
}
