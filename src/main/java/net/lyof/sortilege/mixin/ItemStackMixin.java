package net.lyof.sortilege.mixin;

import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract ItemStack copy();

    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    public void enchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack itemstack = this.copy();
        int a = ItemHelper.getEnchantValue(itemstack);
        int limit = ItemHelper.getMaxEnchantValue(itemstack);
        if (limit >= 0) {
            if (!this.getOrCreateNbt().contains("Enchantments", 9)) {
                this.getOrCreateNbt().put("Enchantments", new NbtList());
            }

            if (a < limit) {
                NbtList listtag = this.getOrCreateNbt().getList("Enchantments", 10);
                listtag.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), (byte) level));
            }

            ci.cancel();
        }
    }
}
