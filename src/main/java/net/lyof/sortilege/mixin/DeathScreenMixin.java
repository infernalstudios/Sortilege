package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.mixin.accessor.ScreenAccessor;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Shadow private Text scoreText;

    @Inject(method = "init", at = @At("TAIL"))
    public void replaceDeathScore(CallbackInfo ci) {
        if (ConfigEntries.showDeathCoordinates) {
            DeathScreen self = (DeathScreen) (Object) this;
            this.scoreText = Text.translatable("sortilege.death_screen.position")
                    .append(Text.literal(" " + ((ScreenAccessor) self).getClient().player.getBlockPos().toShortString())
                            .formatted(Formatting.YELLOW));

            ((ScreenAccessor) self).getClient().player.sendMessage(Text.translatable("sortilege.death_screen.position")
                    .append(Text.literal(" " + ((ScreenAccessor) self).getClient().player.getBlockPos().toShortString())
                            .formatted(Formatting.YELLOW)), false);
        }
    }
}
