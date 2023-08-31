package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ModCommonConfigs;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Inject(method = "setEnchantments", at = @At("HEAD"), cancellable = true)
    private static void setEnchantments(Map<Enchantment, Integer> enchants, ItemStack itemstack, CallbackInfo ci) {
        int limit = ItemHelper.getMaxEnchantValue(itemstack);
        if (limit >= 0) {
            ListTag listtag = new ListTag();

            for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int i = entry.getValue();

                if (enchantment != null) {
                    if (listtag.size() < limit || itemstack.is(Items.ENCHANTED_BOOK)) {
                        listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), i));
                        if (itemstack.is(Items.ENCHANTED_BOOK)) {
                            EnchantedBookItem.addEnchantment(itemstack, new EnchantmentInstance(enchantment, i));
                        }
                    }
                }
            }

            if (listtag.isEmpty()) {
                itemstack.removeTagKey("Enchantments");
            } else if (!itemstack.is(Items.ENCHANTED_BOOK)) {
                itemstack.addTagElement("Enchantments", listtag);
            }
            ci.cancel();
        }
    }
}
