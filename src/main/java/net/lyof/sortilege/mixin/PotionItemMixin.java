package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @WrapMethod(method = "getMaxUseTime")
    public int getConfiguredUseTime(ItemStack stack, Operation<Integer> original) {
        if (PotionUtil.getPotion(stack).getEffects().isEmpty()) return original.call(stack);

        CustomPotionData data = CustomPotionData.get(PotionUtil.getPotion(stack));
        if (data != null) return data.drinkingTime;
        return ConfigEntries.potionDrinkingTime <= 0 ? original.call(stack) : ConfigEntries.potionDrinkingTime;
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void appendDrinkingTime(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (PotionUtil.getPotion(stack).getEffects().isEmpty()) return;

        int drinkingTime = ConfigEntries.potionDrinkingTime;
        int cooldown = ConfigEntries.potionCooldown;
        CustomPotionData data = CustomPotionData.get(PotionUtil.getPotion(stack));
        if (data != null) {
            drinkingTime = data.drinkingTime;
            cooldown = data.cooldown;
        }

        if (stack.isOf(Items.POTION))
            tooltip.add(Text.translatable("sortilege.potion.drinking_time", drinkingTime / 20f)
                    .formatted(Formatting.GRAY));
        if (cooldown > 0)
            tooltip.add(Text.translatable("sortilege.staff.cooldown", cooldown / 20f)
                .formatted(Formatting.GRAY));
    }
}
