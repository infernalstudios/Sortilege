package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @WrapMethod(method = "getUseDuration")
    public int getConfiguredUseTime(ItemStack stack, LivingEntity entity, Operation<Integer> original) {
        if (!PotionHelper.hasEffects(stack)) return original.call(stack, entity);

        CustomPotionData data = CustomPotionData.get(stack.get(DataComponents.POTION_CONTENTS));
        if (data != null) return data.drinkingTime;
        return ModConfig.potionDrinkingTime.get() <= 0 ? original.call(stack, entity) : ModConfig.potionDrinkingTime.get();
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendDrinkingTime(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                   TooltipFlag flag, CallbackInfo ci) {
        if (!PotionHelper.hasEffects(stack)) return;
        if (!ModConfig.potionTooltip.get()) return;

        int drinkingTime = ModConfig.potionDrinkingTime.get();
        int cooldown = ModConfig.potionCooldown.get();
        CustomPotionData data = CustomPotionData.get(stack.get(DataComponents.POTION_CONTENTS));
        if (data != null) {
            drinkingTime = data.drinkingTime;
            cooldown = data.cooldown;
        }

        if (stack.is(Items.POTION))
            tooltip.add(Component.translatable("tooltip.sortilege.potion.drinking_time", drinkingTime / 20f)
                    .withStyle(ChatFormatting.GRAY));
        if (cooldown > 0)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cooldown", cooldown / 20f)
                .withStyle(ChatFormatting.GRAY));
    }
}
