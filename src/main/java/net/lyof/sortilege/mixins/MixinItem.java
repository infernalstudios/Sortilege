package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class MixinItem {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void showEnchantLimit(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag, CallbackInfo ci) {
        int a = EnchantmentHelper.getEnchantments(itemstack).size();
        int m = ItemHelper.getMaxEnchantValue(itemstack);

        if ((a > 0 || ItemHelper.getExtraEnchants(itemstack) > 0 || ConfigEntries.AlwaysShowEnchantLimit) &&
                m > 0 && itemstack.getEnchantmentValue() > 0 && !itemstack.is(Items.ENCHANTED_BOOK))

            list.add(Component.literal(a + "/" + m).append(" ")
                    .append(Component.translatable("sortilege.enchantments"))
                    .withStyle(a >= m ? ChatFormatting.RED : ChatFormatting.WHITE));
    }
}
