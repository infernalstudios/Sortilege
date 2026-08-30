package net.lyof.sortilege.mixin;

import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LingeringPotionItem.class)
public class LingeringPotionItemMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendDrinkingTime(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        if (!PotionHelper.hasEffects(stack)) return;
        if (!ModConfig.potionTooltip.get()) return;

        int cooldown = ModConfig.potionCooldown.get();
        CustomPotionData data = CustomPotionData.get(stack.get(DataComponents.POTION_CONTENTS));
        if (data != null) cooldown = data.cooldown;

        if (cooldown > 0)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cooldown", cooldown / 20f).withStyle(ChatFormatting.GRAY));
    }
}
