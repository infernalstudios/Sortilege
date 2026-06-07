package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow private ServerPlayer player;

    @WrapOperation(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/Advancement;getChatComponent()Lnet/minecraft/network/chat/Component;"))
    public Component formatGetWoodenStaffAdvancement(Advancement instance, Operation<Component> original) {
        if (instance.getDisplay().getTitle().getString().contains("{playerName}")) {
            Component text = Component.literal(instance.getDisplay().getTitle().getString().replace("{playerName}", this.player.getDisplayName().getString()));
            ChatFormatting formatting = instance.getDisplay().getFrame().getChatColor();
            Component text2 = ComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withColor(formatting)).append("\n").append(instance.getDisplay().getDescription());
            Component text3 = text.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2)));
            return ComponentUtils.wrapInSquareBrackets(text3).withStyle(formatting);
        }
        return original.call(instance);
    }
}
