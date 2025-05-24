package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void setEnchantments(Map<Enchantment, Integer> enchants, ItemStack itemstack, CallbackInfo ci) {
        int limit = ItemHelper.getTotalEnchantSlots(itemstack);
        int a = 0;
        if (limit >= 0) {
            NbtList listtag = new NbtList();

            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int i = entry.getValue();

                if (enchantment != null) {
                    if (a < limit || itemstack.isOf(Items.ENCHANTED_BOOK) || (ConfigEntries.cursesAddSlots && enchantment.isCursed())) {
                        listtag.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), i));
                        if (itemstack.isOf(Items.ENCHANTED_BOOK)) {
                            EnchantedBookItem.addEnchantment(itemstack, new EnchantmentLevelEntry(enchantment, i));
                        }

                        if (!enchantment.isCursed() || !ConfigEntries.cursesAddSlots) a++;
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

    @Inject(method = "calculateRequiredExperienceLevel", at = @At("HEAD"), cancellable = true)
    private static void betterEnchantingCosts(Random random, int slotIndex, int bookshelfCount, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (ConfigEntries.doIncreasedEnchantCosts && ConfigEntries.increasedEnchantNeeds.size() == 3) {
            int bonus = (int) ((slotIndex * 0.5) * bookshelfCount);
            cir.setReturnValue(ConfigEntries.increasedEnchantNeeds.get(slotIndex).intValue() + bonus);
        }
    }
}
