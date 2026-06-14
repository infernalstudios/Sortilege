package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @WrapMethod(method = "getUseDuration")
    public int getConfiguredUseTime(ItemStack stack, Operation<Integer> original) {
        if (PotionUtils.getPotion(stack).getEffects().isEmpty()) return original.call(stack);

        CustomPotionData data = CustomPotionData.get(PotionUtils.getPotion(stack));
        if (data != null) return data.drinkingTime;
        return ModConfig.potionDrinkingTime.get() <= 0 ? original.call(stack) : ModConfig.potionDrinkingTime.get();
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendDrinkingTime(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context, CallbackInfo ci) {
        if (PotionUtils.getPotion(stack).getEffects().isEmpty()) return;
        if (!ModConfig.potionTooltip.get()) return;

        int drinkingTime = ModConfig.potionDrinkingTime.get();
        int cooldown = ModConfig.potionCooldown.get();
        CustomPotionData data = CustomPotionData.get(PotionUtils.getPotion(stack));
        if (data != null) {
            drinkingTime = data.drinkingTime;
            cooldown = data.cooldown;
        }

        if (stack.is(Items.POTION))
            tooltip.add(Component.translatable("sortilege.potion.drinking_time", drinkingTime / 20f)
                    .withStyle(ChatFormatting.GRAY));
        if (cooldown > 0)
            tooltip.add(Component.translatable("sortilege.staff.cooldown", cooldown / 20f)
                .withStyle(ChatFormatting.GRAY));
    }
}
