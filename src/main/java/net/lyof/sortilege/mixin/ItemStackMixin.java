package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.crafting.EnchantingCatalyst;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract ItemStack copy();

    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Shadow public abstract boolean isIn(TagKey<Item> tag);

    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    public void enchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack itemstack = this.copy();
        int a = ItemHelper.getUsedEnchantSlots(itemstack);
        int limit = ItemHelper.getTotalEnchantSlots(itemstack);
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

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V"))
    public void showEnchantLimit(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir,
                                 @Local List<Text> list) {
        ItemStack self = (ItemStack) (Object) this;

        int a = ItemHelper.getUsedEnchantSlots(self);
        int m = ItemHelper.getTotalEnchantSlots(self);

        if ((a > 0 || ItemHelper.getExtraEnchantSlots(self) > 0 || ConfigEntries.alwaysShowEnchantLimit) &&
                m > 0 && self.getItem().getEnchantability() > 0 && !self.isOf(Items.ENCHANTED_BOOK)) {

            MutableText txt = Text.translatableWithFallback("sortilege.enchantments.limit." + a + "." + m,
                    a + "/" + m + " " + Text.translatable("sortilege.enchantments").getString());

            if (list.size() > 1)
                list.add(Text.empty());
            list.add(txt.formatted(a >= m ? Formatting.RED : Formatting.WHITE));
        }
    }
}
