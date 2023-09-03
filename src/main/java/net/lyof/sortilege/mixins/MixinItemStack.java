package net.lyof.sortilege.mixins;

import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract ItemStack copy();
    @Shadow public abstract CompoundTag getOrCreateTag();

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    public void enchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack itemstack = this.copy();
        int a = EnchantmentHelper.getEnchantments(itemstack).size();
        int limit = ItemHelper.getMaxEnchantValue(itemstack);
        if (limit >= 0) {
            if (!this.getOrCreateTag().contains("Enchantments", 9)) {
                this.getOrCreateTag().put("Enchantments", new ListTag());
            }

            if (a < limit) {
                ListTag listtag = this.getOrCreateTag().getList("Enchantments", 10);
                listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), (byte) level));
            }

            ci.cancel();
        }
    }
}
