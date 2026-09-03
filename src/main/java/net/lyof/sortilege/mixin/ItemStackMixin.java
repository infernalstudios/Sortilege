package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.enchant.IBuiltinEnchantsItem;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.item.potion.PotionCooldownManager;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean is(TagKey<Item> tag);
    @Shadow public abstract Item getItem();
    @Shadow public abstract void enchant(Holder<Enchantment> enchantment, int level);

    @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private MutableComponent changeRarityFormatting(MutableComponent instance, ChatFormatting formatting, Operation<MutableComponent> original) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.is(ModTags.Items.FORGOTTEN_ITEMS))
            return instance.withStyle(ChatFormatting.GREEN);
        return original.call(instance, formatting);
    }

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    public void enchantWithLimit(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;

        int a = EnchantHelper.getUsedEnchantSlots(self);
        int limit = EnchantHelper.getTotalEnchantSlots(self);
        if (limit >= 0) {
            EnchantmentHelper.updateEnchantments(self, mutable -> {
                if (mutable.getLevel(enchantment) > 0 || a < limit
                        || (ModConfig.cursesAddSlots.get() && enchantment.is(EnchantmentTags.CURSE))) {
                    mutable.upgrade(enchantment, level);
                }
            });

            ci.cancel();
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL"))
    private void builtinEnchants(ItemLike item, int count, CallbackInfo ci) {
        if (item.asItem() instanceof IBuiltinEnchantsItem builtin) {
            for (Map.Entry<ResourceLocation, Integer> entry : builtin.getBuiltinEnchantments().entrySet()) {
                /* TODO Enchantment enchant = BuiltInRegistries.ENCHANTMENT.get(entry.getKey());
                if (enchant != null)
                    this.enchant(enchant, entry.getValue());*/
            }
        }
    }

    @ModifyReturnValue(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
    private boolean isInKinetic(boolean original, TagKey<Item> tag) {
        if (tag.equals(ModTags.Items.KINETIC_BOOSTED) && this.getItem() instanceof AStaffItem staff)
            return original && staff.canMelee((ItemStack) (Object) this);
        return original;
    }

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    public void unbreakableTag(CallbackInfoReturnable<Boolean> cir) {
        if (this.is(ModTags.Items.UNBREAKABLE)) cir.setReturnValue(false);
        if (ModConfig.expandedUnbreaking.get() > 0 &&
                EnchantHelper.getEnchantLevel(Enchantments.UNBREAKING, (ItemStack) (Object) this) >= ModConfig.expandedUnbreaking.get())
            cir.setReturnValue(false);
    }

    @WrapMethod(method = "getMaxStackSize")
    public int stackablePotions(Operation<Integer> original) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PotionHelper.isPotionItem(self)) return original.call();

        int stackSize = ModConfig.potionStackSize.get();
        CustomPotionData data = CustomPotionData.get(self.get(DataComponents.POTION_CONTENTS));
        if (data != null) stackSize = data.stackSize;

        return stackSize;
    }


    @Unique
    private void sorti_setPotionCooldown(ItemStack self, LivingEntity user) {
        int cooldown = ModConfig.potionCooldown.get();
        CustomPotionData data = CustomPotionData.get(self.get(DataComponents.POTION_CONTENTS));
        if (data != null) cooldown = data.cooldown;

        PotionCooldownManager.set(self, user, cooldown);
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    public void putOnCooldown(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PotionHelper.isPotionItem(self) || !PotionHelper.hasEffects(self)) return;

        this.sorti_setPotionCooldown(self, user);
    }

    @WrapMethod(method = "use")
    public InteractionResultHolder<ItemStack> handleUse(Level world, Player user, InteractionHand hand,
                                                  Operation<InteractionResultHolder<ItemStack>> original) {
        ItemStack self = (ItemStack) (Object) this;

        if (!PotionHelper.isPotionItem(self)) return original.call(world, user, hand);
        if (PotionCooldownManager.getProgress(self, user, 0) > 0) return InteractionResultHolder.fail(self);

        if (self.getItem() instanceof ThrowablePotionItem && PotionHelper.hasEffects(self))
            this.sorti_setPotionCooldown(self, user);

        return original.call(world, user, hand);
    }
}
