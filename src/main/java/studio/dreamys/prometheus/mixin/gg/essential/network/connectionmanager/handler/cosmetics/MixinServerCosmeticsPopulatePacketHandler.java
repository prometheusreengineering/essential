package studio.dreamys.prometheus.mixin.gg.essential.network.connectionmanager.handler.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.essential.connectionmanager.common.packet.cosmetic.ServerCosmeticsPopulatePacket;
import gg.essential.cosmetics.model.Cosmetic;
import gg.essential.network.connectionmanager.ConnectionManager;
import gg.essential.network.connectionmanager.cosmetics.CosmeticsManager;
import gg.essential.network.connectionmanager.handler.cosmetics.ServerCosmeticsPopulatePacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(value = ServerCosmeticsPopulatePacketHandler.class, remap = false)
public class MixinServerCosmeticsPopulatePacketHandler {
    @Unique
    private static final Gson essential$gson = new GsonBuilder().setPrettyPrinting().create();

    @SuppressWarnings("rawtypes")
    @Inject(method = "onHandle(Lgg/essential/network/connectionmanager/ConnectionManager;Lgg/essential/connectionmanager/common/packet/cosmetic/ServerCosmeticsPopulatePacket;)V", at = @At(value = "INVOKE", target = "Lgg/essential/cosmetics/model/Cosmetic;getType()Ljava/lang/String;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onHandle(ConnectionManager connectionManager, ServerCosmeticsPopulatePacket packet, CallbackInfo ci, CosmeticsManager cosmeticsManager, Iterator var4, Cosmetic cosmetic) {
        System.out.println(essential$gson.toJson(cosmetic));
    }
}
