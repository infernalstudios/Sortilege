package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract ItemStack copy();

    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Shadow public abstract boolean isIn(TagKey<Item> tag);

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

    @Inject(method = "isDamageable", at = @At("HEAD"), cancellable = true)
    public void unbreakableTag(CallbackInfoReturnable<Boolean> cir) {
        if (this.isIn(ModTags.Items.UNBREAKABLE)) cir.setReturnValue(false);
        if (EnchantmentHelper.getLevel(Enchantments.UNBREAKING, (ItemStack) (Object) this) >= ConfigEntries.betterUnbreaking)
            cir.setReturnValue(false);
    }
}
