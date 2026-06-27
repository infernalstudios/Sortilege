package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.potion.PotionCooldownManager;
import net.lyof.sortilege.item.potion.PotionShenanigans;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.XPHelper;
import net.lyof.sortilege.util.inject.BountyHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements PotionShenanigans, BountyHolder {
    @Unique private static final String BOUNTY_KEY = "sorti_StolenXP";

    @Unique private final Map<MobEffect, Integer> effectImmunities = new HashMap<>();
    @Unique private int stolenxp = 0;

    @Override
    public void sorti_setImmunity(MobEffect effect, int time) {
        int timeOff = this.tickCount + time;
        if (this.effectImmunities.containsKey(effect)) {
            if (this.effectImmunities.get(effect) < timeOff)
                this.effectImmunities.replace(effect, timeOff);
        }
        else
            this.effectImmunities.put(effect, timeOff);
    }

    @Override
    public void sorti_setExperience(int i) {
        this.stolenxp = i;
    }

    @Override
    public int sorti_getExperience() {
        return this.stolenxp;
    }

    @Shadow public abstract boolean hurt(DamageSource source, float amount);
    @Shadow public abstract RandomSource getRandom();
    @Shadow @Nullable protected Player lastHurtByPlayer;
    @Shadow public abstract ItemStack getOffhandItem();
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustom(CompoundTag nbt, CallbackInfo ci) {
        nbt.putInt(BOUNTY_KEY, this.sorti_getExperience());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustom(CompoundTag nbt, CallbackInfo ci) {
        this.sorti_setExperience(nbt.getInt(BOUNTY_KEY));
    }

    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    public int xpDropBonus(int amount) {
        if (ModConfig.witchHatEnabled.get() && this.lastHurtByPlayer != null && this.lastHurtByPlayer.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.WITCH_HAT))
            amount += ModConfig.witchHatBonus.get();
        if (this.getType().is(ModTags.Entities.UNEXPERIENCED))
            amount = 0;

        return amount;
    }

    @Inject(method = "dropFromLootTable", at = @At("HEAD"))
    public void witchHatDrop(DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if (causedByPlayer && this.getType() == EntityType.WITCH) {
            Level world = this.level();
            if (world.isClientSide()) return;

            if (!ModConfig.witchHatEnabled.get() || Math.random() > ModConfig.witchHatDropChance.get()) return;

            ItemStack hat = ModItems.WITCH_HAT.getDefaultInstance();
            hat.setDamageValue((int) Math.round(Math.random() * (hat.getMaxDamage() - 10)) + 10);
            world.addFreshEntity(new ItemEntity(world, this.getX(), this.getY(), this.getZ(), hat));
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    public void giveKillXP(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        PotionCooldownManager.clear(self);

        if (self instanceof Player player && this.level() instanceof ServerLevel world) {
            int stealxp = (int) Math.round(XPHelper.getTotalXP(player.experienceLevel, player.experienceProgress, world) * ModConfig.attackerXPRatio.get());
            Entity source = damageSource.getEntity();

            if (source instanceof Player playerattacker)
                playerattacker.giveExperiencePoints(stealxp);
            else if (source instanceof LivingEntity attacker) {
                ((BountyHolder) attacker).sorti_setExperience(stealxp);
                if (ModConfig.glowingKiller.get()) attacker.setGlowingTag(true);
            }
            else ExperienceOrb.award(world, this.position(), stealxp);
        }

        if (this.level() instanceof ServerLevel world)
            ExperienceOrb.award(world, this.position(), this.sorti_getExperience());

        if (self instanceof Enemy && Math.random() < ModConfig.bountyChance.get()
                && (ModConfig.bountyTagWhitelist.get() == self.getType().is(ModTags.Entities.BOUNTIES))
                && damageSource.getEntity() instanceof Player player) {

            if (player.level() instanceof ServerLevel world)
                ExperienceOrb.award(world, this.position(), ModConfig.bountyValue.get());

            ModParticles.sendParticles(player.level(), this.getX(), this.getY() + this.getEyeHeight(this.getPose()) / 2, this.getZ(),
                    16, new float[]{0.5f, 1f, 0.2f});
        }
    }

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    public void cancelCuriosDrop(CallbackInfo ci) {
        if (ModConfig.keepEquipped.get() && ((LivingEntity) (Object) this) instanceof Player) ci.cancel();
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void cancelDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.expandedFeatherFalling.get() > 0 && source.is(DamageTypeTags.IS_FALL) &&
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FALL_PROTECTION,
                        this.getItemBySlot(EquipmentSlot.FEET)) >= ModConfig.expandedFeatherFalling.get())
            cir.setReturnValue(false);

        if (ModConfig.expandedFireProt.get() > 0 && source.is(DamageTypeTags.IS_FIRE) &&
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_PROTECTION,
                        this.getItemBySlot(EquipmentSlot.FEET)) >= ModConfig.expandedFireProt.get() &&
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_PROTECTION,
                        this.getItemBySlot(EquipmentSlot.LEGS)) >= ModConfig.expandedFireProt.get() &&
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_PROTECTION,
                        this.getItemBySlot(EquipmentSlot.CHEST)) >= ModConfig.expandedFireProt.get() &&
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_PROTECTION,
                        this.getItemBySlot(EquipmentSlot.HEAD)) >= ModConfig.expandedFireProt.get())
            cir.setReturnValue(false);

        if (ModEnchants.MAGIC_PROTECTION != null && ModConfig.expandedMagicProt.get() && Math.random() <=
                0.05 * EnchantmentHelper.getEnchantmentLevel(ModEnchants.MAGIC_PROTECTION, (LivingEntity) (Object) this))
            cir.setReturnValue(false);
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    public void isBlockingWithLapisShield(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getOffhandItem();
        if (!ModConfig.lapisShieldEnabled.get() || !stack.is(ModItems.LAPIS_SHIELD)) return;

        if (!LapisShieldItem.isOnCooldown(stack))
            cir.setReturnValue(true);
    }

    @WrapOperation(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;dot(Lnet/minecraft/world/phys/Vec3;)D"))
    public double blockedByLapisShield(Vec3 a, Vec3 b, Operation<Double> original) {
        double v = original.call(a, b);
        if (!ModConfig.lapisShieldEnabled.get() || !this.getOffhandItem().is(ModItems.LAPIS_SHIELD)) return v;

        if (v <= -0.5 * Math.cos(90 * Math.PI / 360d))
            return -1;
        return 1;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickLapisShield(CallbackInfo ci) {
        ItemStack stack = this.getOffhandItem();
        if (!ModConfig.lapisShieldEnabled.get() || !stack.is(ModItems.LAPIS_SHIELD) || !LapisShieldItem.isOnCooldown(stack)
                || this.level().isClientSide()) return;

        if (LapisShieldItem.getCooldownEnd(stack) <= this.tickCount
                || LapisShieldItem.getCooldownEnd(stack) - ModConfig.lapisShieldCooldown.get() - 1 > this.tickCount) {
            LapisShieldItem.removeCooldown(stack);
            LapisShieldItem.sendCooldownUpdate((LivingEntity) (Object) this, 0);
        }
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    public void damageLapisShield(float amount, CallbackInfo ci) {
        ItemStack stack = this.getOffhandItem();
        if (!ModConfig.lapisShieldEnabled.get() || !stack.is(ModItems.LAPIS_SHIELD)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        LapisShieldItem.onSuccessfulUse(stack, self, amount);
    }

    @WrapMethod(method = "canBeAffected")
    public boolean applyEffectImmunity(MobEffectInstance effect, Operation<Boolean> original) {
        if (this.effectImmunities.containsKey(effect.getEffect())) {
            if (this.effectImmunities.get(effect.getEffect()) >= this.tickCount) {
                return false;
            }
            else
                this.effectImmunities.remove(effect.getEffect());
        }
        return original.call(effect);
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder addGlobalAttributes(AttributeSupplier.Builder original) {
        for (Attribute attribute : ModAttributes.GLOBALS)
            original.add(attribute);
        return original;
    }
}
