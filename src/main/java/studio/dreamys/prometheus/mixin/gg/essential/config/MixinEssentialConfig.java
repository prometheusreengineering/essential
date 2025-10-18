package studio.dreamys.prometheus.mixin.gg.essential.config;

import gg.essential.config.EssentialConfig;
import gg.essential.gui.elementa.state.v2.MutableState;
import gg.essential.gui.elementa.state.v2.StateKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EssentialConfig.class, remap = false)
public class MixinEssentialConfig {
    @Redirect(method = "getDisableCosmetics", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;booleanValue()Z"))
    public boolean getDisableCosmetics(Boolean instance) {
        return false;
    }

    @Redirect(method = "getHideCosmeticsWhenServerOverridesSkinState", at = @At(value = "FIELD", target = "Lgg/essential/config/EssentialConfig;hideCosmeticsWhenServerOverridesSkinState:Lgg/essential/gui/elementa/state/v2/MutableState;"))
    public MutableState<Boolean> getHideCosmeticsWhenServerOverridesSkinState() {
        return StateKt.mutableStateOf(true);
    }
}
