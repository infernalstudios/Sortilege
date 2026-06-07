package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DisplayInfo.class)
public abstract class DisplayInfoMixin {
    @ModifyReturnValue(method = "getTitle", at = @At(value = "RETURN"))
    public Component formatGetWoodenStaffAdvancement(Component original) {
        Component text = original;
        if (text.getString().contains("{playerName}"))
            text = Component.literal(text.getString().replace("{playerName}", Minecraft.getInstance().getUser().getName()));
        return text;
    }
}
