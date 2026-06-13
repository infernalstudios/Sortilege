package net.lyof.sortilege.mixin;

import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "setEnchantments", at = @At("HEAD"), cancellable = true)
    private static void setEnchantments(Map<Enchantment, Integer> enchants, ItemStack itemstack, CallbackInfo ci) {
        int limit = EnchantHelper.getTotalEnchantSlots(itemstack);
        int a = 0;
        if (limit >= 0) {
            ListTag listtag = new ListTag();

            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int i = entry.getValue();

                if (enchantment != null) {
                    if (a < limit || itemstack.is(Items.ENCHANTED_BOOK) || (ModConfig.cursesAddSlots.get() && enchantment.isCurse())) {
                        listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), i));
                        if (itemstack.is(Items.ENCHANTED_BOOK)) {
                            EnchantedBookItem.addEnchantment(itemstack, new EnchantmentInstance(enchantment, i));
                        }

                        if (!enchantment.isCurse() || !ModConfig.cursesAddSlots.get()) a++;
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

    @Inject(method = "getEnchantmentCost", at = @At("HEAD"), cancellable = true)
    private static void betterEnchantingCosts(RandomSource random, int slotIndex, int bookshelfCount, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.doIncreasedEnchantCosts.get() && ModConfig.increasedEnchantNeeds.get().size() == 3) {
            int bonus = (int) ((slotIndex * 0.5) * bookshelfCount);
            cir.setReturnValue(ModConfig.increasedEnchantNeeds.get().get(slotIndex) + bonus);
        }
    }
}
