package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract ItemStack copy();
    @Shadow public abstract CompoundTag getOrCreateTag();

    @Shadow public abstract boolean is(TagKey<Item> p_204118_);

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    public void enchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack itemstack = this.copy();
        int a = ItemHelper.getEnchantValue(itemstack);
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

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    public void unbreakableTag(CallbackInfoReturnable<Boolean> cir) {
        if (this.is(ModTags.Items.UNBREAKABLE)) cir.setReturnValue(false);
        if (ItemHelper.getEnchantLevel(Enchantments.UNBREAKING, (ItemStack) (Object) this) >= 3 && ConfigEntries.betterUnbreaking)
            cir.setReturnValue(false);
    }
}
