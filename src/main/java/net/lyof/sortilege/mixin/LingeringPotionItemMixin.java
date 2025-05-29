package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LingeringPotionItem.class)
public class LingeringPotionItemMixin {
    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void appendDrinkingTime(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        int cooldown = ConfigEntries.potionCooldown;
        CustomPotionData data = CustomPotionData.get(PotionUtil.getPotion(stack));
        if (data != null) cooldown = data.cooldown;

        if (cooldown > 0)
            tooltip.add(Text.translatable("sortilege.staff.cooldown", cooldown / 20f)
                    .formatted(Formatting.GRAY));
    }
}
