package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.staff.StaffItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract CompoundTag getOrCreateTag();
    @Shadow public abstract boolean is(TagKey<Item> tag);
    @Shadow public abstract Item getItem();

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    public void enchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;

        int a = EnchantHelper.getUsedEnchantSlots(self);
        int limit = EnchantHelper.getTotalEnchantSlots(self);
        if (limit >= 0) {
            if (!this.getOrCreateTag().contains("Enchantments", 9))
                this.getOrCreateTag().put("Enchantments", new ListTag());

            if (a < limit || (ModConfig.cursesAddSlots.get() && enchantment.isCurse())) {
                ListTag listtag = this.getOrCreateTag().getList("Enchantments", 10);
                listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), (byte) level));
            }

            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
    private boolean isInKinetic(boolean original, TagKey<Item> tag) {
        if (tag.equals(ModTags.Items.KINETIC_BOOSTED) && this.getItem() instanceof StaffItem)
            return original && EnchantHelper.hasEnchant(ModEnchants.BONK, (ItemStack) (Object) this);
        return original;
    }

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    public void unbreakableTag(CallbackInfoReturnable<Boolean> cir) {
        if (this.is(ModTags.Items.UNBREAKABLE)) cir.setReturnValue(false);
        if (ModConfig.expandedUnbreaking.get() > 0 && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, (ItemStack) (Object) this) >= ModConfig.expandedUnbreaking.get())
            cir.setReturnValue(false);
    }

    @WrapMethod(method = "getMaxStackSize")
    public int stackablePotions(Operation<Integer> original) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PotionHelper.isPotionItem(self)) return original.call();

        int stackSize = ModConfig.potionStackSize.get();
        CustomPotionData data = CustomPotionData.get(PotionUtils.getPotion(self));
        if (data != null) stackSize = data.stackSize;

        return stackSize;
    }


    @Unique
    private void sorti_setPotionCooldown(ItemStack self, LivingEntity user) {
        int cooldown = ModConfig.potionCooldown.get();
        CustomPotionData data = CustomPotionData.get(PotionUtils.getPotion(self));
        if (data != null) cooldown = data.cooldown;

        PotionCooldownManager.set(self, user, cooldown);
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    public void putOnCooldown(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PotionHelper.isPotionItem(self) || PotionUtils.getPotion(self).getEffects().isEmpty()) return;

        this.sorti_setPotionCooldown(self, user);
    }

    @WrapMethod(method = "use")
    public InteractionResultHolder<ItemStack> handleUse(Level world, Player user, InteractionHand hand,
                                                  Operation<InteractionResultHolder<ItemStack>> original) {
        ItemStack self = (ItemStack) (Object) this;

        if (!PotionHelper.isPotionItem(self)) return original.call(world, user, hand);
        if (PotionCooldownManager.getProgress(self, user, 0) > 0) return InteractionResultHolder.fail(self);

        if (self.getItem() instanceof ThrowablePotionItem && !PotionUtils.getPotion(self).getEffects().isEmpty())
            this.sorti_setPotionCooldown(self, user);

        return original.call(world, user, hand);
    }
}
