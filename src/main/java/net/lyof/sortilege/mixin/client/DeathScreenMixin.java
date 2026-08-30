package net.lyof.sortilege.mixin.client;

import net.lyof.sortilege.mixin.accessor.ScreenAccessor;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Shadow private Component deathScore;

    @Inject(method = "init", at = @At("TAIL"))
    public void replaceDeathScore(CallbackInfo ci) {
        if (ModConfig.showDeathCoordinates.get()) {
            DeathScreen self = (DeathScreen) (Object) this;
            this.deathScore = Component.translatable("screen.sortilege.death_screen.position")
                    .append(Component.literal(" " + ((ScreenAccessor) self).getMinecraft().player.blockPosition().toShortString())
                            .withStyle(ChatFormatting.YELLOW));

            ((ScreenAccessor) self).getMinecraft().player.displayClientMessage(Component.translatable("screen.sortilege.death_screen.position")
                    .append(Component.literal(" " + ((ScreenAccessor) self).getMinecraft().player.blockPosition().toShortString())
                            .withStyle(ChatFormatting.YELLOW)), false);
        }
    }
}
