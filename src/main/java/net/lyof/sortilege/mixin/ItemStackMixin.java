package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.StaffItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract ItemStack copy();
    @Shadow public abstract NbtCompound getOrCreateNbt();
    @Shadow public abstract boolean isIn(TagKey<Item> tag);

    @Shadow public abstract Item getItem();

    @Inject(method = "addEnchantment", at = @At("HEAD"), cancellable = true)
    public void enchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack itemstack = this.copy();
        int a = ItemHelper.getUsedEnchantSlots(itemstack);
        int limit = ItemHelper.getTotalEnchantSlots(itemstack);
        if (limit >= 0) {
            if (!this.getOrCreateNbt().contains("Enchantments", 9))
                this.getOrCreateNbt().put("Enchantments", new NbtList());

            if (a < limit || (ConfigEntries.cursesAddSlots && enchantment.isCursed())) {
                NbtList listtag = this.getOrCreateNbt().getList("Enchantments", 10);
                listtag.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), (byte) level));
            }

            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "isIn", at = @At("RETURN"))
    private boolean isInKinetic(boolean original, TagKey<Item> tag) {
        if (tag.equals(ModTags.Items.KINETIC_BOOSTED) && this.getItem() instanceof StaffItem)
            return original && ItemHelper.hasEnchant(ModEnchants.BONK, (ItemStack) (Object) this);
        return original;
    }

    @Inject(method = "isDamageable", at = @At("HEAD"), cancellable = true)
    public void unbreakableTag(CallbackInfoReturnable<Boolean> cir) {
        if (this.isIn(ModTags.Items.UNBREAKABLE)) cir.setReturnValue(false);
        if (ConfigEntries.betterUnbreaking > 0 && EnchantmentHelper.getLevel(Enchantments.UNBREAKING, (ItemStack) (Object) this) >= ConfigEntries.betterUnbreaking)
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

            if (list.size() > 1 && !"".equals(list.get(list.size() - 1).getString()))
                list.add(Text.empty());
            list.add(txt.formatted(a >= m ? Formatting.RED : Formatting.WHITE));
        }
    }

    @WrapMethod(method = "getMaxCount")
    public int stackablePotions(Operation<Integer> original) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PotionHelper.isPotionItem(self)) return original.call();

        int stackSize = ConfigEntries.potionStackSize;
        CustomPotionData data = CustomPotionData.get(PotionUtil.getPotion(self));
        if (data != null) stackSize = data.stackSize;

        return stackSize;
    }


    @Unique
    private void setPotionCooldown(ItemStack self, LivingEntity user) {
        int cooldown = ConfigEntries.potionCooldown;
        CustomPotionData data = CustomPotionData.get(PotionUtil.getPotion(self));
        if (data != null) cooldown = data.cooldown;

        PotionCooldownManager.set(self, user, cooldown);
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void putOnCooldown(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PotionHelper.isPotionItem(self) || PotionUtil.getPotion(self).getEffects().isEmpty()) return;

        this.setPotionCooldown(self, user);
    }

    @WrapMethod(method = "use")
    public TypedActionResult<ItemStack> handleUse(World world, PlayerEntity user, Hand hand,
                                                  Operation<TypedActionResult<ItemStack>> original) {
        ItemStack self = (ItemStack) (Object) this;

        if (user != null) {
            EnchantKnowledge knowledge = ((EnchantLearner) user).sorti_getKnowledge();
            Sortilege.log(knowledge);
            if (knowledge != null) knowledge.learn(self);
        }

        if (!PotionHelper.isPotionItem(self)) return original.call(world, user, hand);
        if (PotionCooldownManager.getProgress(self, user, 0) > 0) return TypedActionResult.fail(self);

        if (self.getItem() instanceof ThrowablePotionItem && !PotionUtil.getPotion(self).getEffects().isEmpty())
            this.setPotionCooldown(self, user);

        return original.call(world, user, hand);
    }
}
