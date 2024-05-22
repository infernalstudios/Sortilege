package net.lyof.sortilege.mixin;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void showEnchantLimit(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        int a = ItemHelper.getEnchantValue(stack);
        int m = ItemHelper.getMaxEnchantValue(stack);

        if ((a > 0 || ItemHelper.getExtraEnchants(stack) > 0 || ConfigEntries.alwaysShowEnchantLimit) &&
                m > 0 && stack.getItem().getEnchantability() > 0 && !stack.isOf(Items.ENCHANTED_BOOK))

            tooltip.add(Text.literal(a + "/" + m).append(" ")
                    .append(Text.translatable("sortilege.enchantments"))
                    .formatted(a >= m ? Formatting.RED : Formatting.WHITE));
    }
}
