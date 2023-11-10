package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class MixinDeathScreen {
    @Shadow private Component deathScore;

    @Inject(method = "init", at = @At("TAIL"))
    public void replaceDeathScore(CallbackInfo ci) {
        if (ConfigEntries.ShowDeathCoordinates) {
            DeathScreen self = (DeathScreen) (Object) this;
            this.deathScore = Component.translatable("sortilege.death_screen.position")
                    .append(Component.literal(" " + self.getMinecraft().player.blockPosition().toShortString())
                            .withStyle(ChatFormatting.YELLOW));
        }
    }
}
