package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin {
    @WrapOperation(method = "addToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;"))
    private Component cacheKnowledge(Holder<Enchantment> enchantment, int level, Operation<Component> original) {
        MutableComponent text = (MutableComponent) original.call(enchantment, level);
        if (EnchantLearner.Cache.isLearnable(enchantment))
            text = text.append(Component.translatable("tooltip.sortilege.learnable").withStyle(ChatFormatting.LIGHT_PURPLE));
        return text;
    }
}
