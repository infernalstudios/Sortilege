package net.lyof.sortilege.mixin;

import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void setEnchantments(Map<Enchantment, Integer> enchants, ItemStack itemstack, CallbackInfo ci) {
        int limit = ItemHelper.getMaxEnchantValue(itemstack);
        if (limit >= 0) {
            NbtList listtag = new NbtList();

            for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int i = entry.getValue();

                if (enchantment != null) {
                    if (listtag.size() < limit || itemstack.isOf(Items.ENCHANTED_BOOK)) {
                        listtag.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), i));
                        if (itemstack.isOf(Items.ENCHANTED_BOOK)) {
                            EnchantedBookItem.addEnchantment(itemstack, new EnchantmentLevelEntry(enchantment, i));
                        }
                    }
                }
            }

            if (listtag.isEmpty()) {
                itemstack.removeSubNbt("Enchantments");
            } else if (!itemstack.isOf(Items.ENCHANTED_BOOK)) {
                itemstack.setSubNbt("Enchantments", listtag);
            }
            ci.cancel();
        }
    }
}
