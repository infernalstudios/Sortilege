package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementDisplay.class)
public abstract class AdvancementDisplayMixin {
    @ModifyReturnValue(method = "getTitle", at = @At(value = "RETURN"))
    public Text formatGetWoodenStaffAdvancement(Text original) {
        Text text = original;
        if (text.getString().contains("{playerName}"))
            text = Text.literal(text.getString().replace("{playerName}", MinecraftClient.getInstance().getSession().getUsername()));
        return text;
    }
}
