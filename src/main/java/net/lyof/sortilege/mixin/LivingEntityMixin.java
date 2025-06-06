package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.lyof.sortilege.item.custom.potion.PotionShenanigans;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
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
public abstract class LivingEntityMixin extends Entity implements PotionShenanigans {
    @Unique private final Map<StatusEffect, Integer> effectImmunities = new HashMap<>();

    @Override
    public void sorti$setImmunity(StatusEffect effect, int time) {
        int timeOff = this.age + time;
        if (this.effectImmunities.containsKey(effect)) {
            if (this.effectImmunities.get(effect) < timeOff)
                this.effectImmunities.replace(effect, timeOff);
        }
        else
            this.effectImmunities.put(effect, timeOff);
    }

    @Shadow @Nullable protected PlayerEntity attackingPlayer;
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
    @Shadow public abstract Iterable<ItemStack> getArmorItems();
    @Shadow public abstract boolean damage(DamageSource source, float amount);
    @Shadow public abstract ItemStack getOffHandStack();
    @Shadow public abstract Random getRandom();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"))
    public int xpDropBonus(int amount) {
        if (ConfigEntries.witchHatEnabled && this.attackingPlayer != null && this.attackingPlayer.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.WITCH_HAT))
            amount += ConfigEntries.witchHatBonus;
        if (this.getType().isIn(ModTags.Entities.UNEXPERIENCED))
            amount = 0;

        return amount;
    }

    @Inject(method = "dropLoot", at = @At("HEAD"))
    public void witchHatDrop(DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if (causedByPlayer && this.getType() == EntityType.WITCH) {
            World world = this.getWorld();
            if (world.isClient()) return;

            if (!ConfigEntries.witchHatEnabled || Math.random() > ConfigEntries.witchHatDropChance) return;

            ItemStack hat = ModItems.WITCH_HAT.getDefaultStack();
            hat.setDamage((int) Math.round(Math.random() * (hat.getMaxDamage() - 10)) + 10);
            world.spawnEntity(new ItemEntity(world, this.getX(), this.getY(), this.getZ(), hat));
        }
    }

    @Inject(method = "initDataTracker", at = @At("HEAD"))
    public void trackXPBounty(CallbackInfo ci) {
        this.getDataTracker().startTracking(XPHelper.BOUNTY, 0);
    }


    @Inject(method = "drop", at = @At("HEAD"))
    public void giveKillXP(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        PotionCooldownManager.clear(self);

        if (self instanceof PlayerEntity player && this.getWorld() instanceof ServerWorld world) {
            int steal_xp = (int) Math.round(XPHelper.getTotalxp(player.experienceLevel, player.experienceProgress, world) * ConfigEntries.attackerXPRatio);
            Entity source = damageSource.getAttacker();

            if (source instanceof PlayerEntity playerattacker)
                playerattacker.addExperience(steal_xp);
            else if (source instanceof LivingEntity attacker) {
                attacker.getDataTracker().set(XPHelper.BOUNTY, steal_xp);
                if (ConfigEntries.glowingKiller) attacker.setGlowing(true);
            }
            else ExperienceOrbEntity.spawn(world, this.getPos(), steal_xp);
        }

        if (this.getDataTracker().containsKey(XPHelper.BOUNTY) && this.getWorld() instanceof ServerWorld world) {
            ExperienceOrbEntity.spawn(world, this.getPos(), this.getDataTracker().get(XPHelper.BOUNTY));
        }

        if (self instanceof Monster && Math.random() < ConfigEntries.bountyChance
                && (ConfigEntries.bountyWhitelist == self.getType().isIn(ModTags.Entities.BOUNTIES))
                && damageSource.getAttacker() instanceof PlayerEntity player) {

            if (player.getWorld() instanceof ServerWorld world)
                ExperienceOrbEntity.spawn(world, this.getPos(), ConfigEntries.bountyValue);

            ModParticles.spawnWisps(player.getWorld(), this.getX(), this.getY() + this.getEyeHeight(this.getPose()) / 2, this.getZ(),
                    16, new float[]{0.5f, 1f, 0.2f});
        }
    }

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    public void cancelCuriosDrop(CallbackInfo ci) {
        if (ConfigEntries.keepEquipped && ((LivingEntity) (Object) this) instanceof PlayerEntity) ci.cancel();
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void cancelDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigEntries.betterFeatherFalling > 0 && source.isIn(DamageTypeTags.IS_FALL) &&
                EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, this.getEquippedStack(EquipmentSlot.FEET)) >=
                        ConfigEntries.betterFeatherFalling)
            cir.setReturnValue(false);

        if (ConfigEntries.betterFireProt > 0 && source.isIn(DamageTypeTags.IS_FIRE) &&
                EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, this.getEquippedStack(EquipmentSlot.FEET)) >=
                        ConfigEntries.betterFireProt &&
                EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, this.getEquippedStack(EquipmentSlot.LEGS)) >=
                        ConfigEntries.betterFireProt &&
                EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, this.getEquippedStack(EquipmentSlot.CHEST)) >=
                        ConfigEntries.betterFireProt &&
                EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, this.getEquippedStack(EquipmentSlot.HEAD)) >=
                        ConfigEntries.betterFireProt)
            cir.setReturnValue(false);

        if (ModEnchants.MAGIC_PROTECTION != null && ConfigEntries.betterMagicProt && Math.random() <=
                0.05 * EnchantmentHelper.getEquipmentLevel(ModEnchants.MAGIC_PROTECTION, (LivingEntity) (Object) this))
            cir.setReturnValue(false);
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    public void isBlockingWithLapisShield(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getOffHandStack();
        if (!ConfigEntries.lapisShieldEnabled || !stack.isOf(ModItems.LAPIS_SHIELD)) return;

        if (!LapisShieldItem.isOnCooldown(stack))
            cir.setReturnValue(true);
    }

    @WrapOperation(method = "blockedByShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;dotProduct(Lnet/minecraft/util/math/Vec3d;)D"))
    public double blockedByLapisShield(Vec3d a, Vec3d b, Operation<Double> original) {
        double v = original.call(a, b);
        if (!ConfigEntries.lapisShieldEnabled || !this.getOffHandStack().isOf(ModItems.LAPIS_SHIELD)) return v;

        if (v <= -0.5 * Math.cos(90 * Math.PI / 360d))
            return -1;
        return 1;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickLapisShield(CallbackInfo ci) {
        ItemStack stack = this.getOffHandStack();
        if (!ConfigEntries.lapisShieldEnabled || !stack.isOf(ModItems.LAPIS_SHIELD) || !LapisShieldItem.isOnCooldown(stack)
                || this.getWorld().isClient()) return;

        if (LapisShieldItem.getCooldownEnd(stack) <= this.age
                || LapisShieldItem.getCooldownEnd(stack) - ConfigEntries.lapisShieldCooldown - 1 > this.age) {
            LapisShieldItem.removeCooldown(stack);
            LapisShieldItem.sendCooldownUpdate((LivingEntity) (Object) this, 0);
        }
    }

    @Inject(method = "damageShield", at = @At("HEAD"))
    public void damageLapisShield(float amount, CallbackInfo ci) {
        ItemStack stack = this.getOffHandStack();
        if (!ConfigEntries.lapisShieldEnabled || !stack.isOf(ModItems.LAPIS_SHIELD)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        LapisShieldItem.onSuccessfulUse(stack, self, amount);
    }

    @WrapMethod(method = "canHaveStatusEffect")
    public boolean applyEffectImmunity(StatusEffectInstance effect, Operation<Boolean> original) {
        if (this.effectImmunities.containsKey(effect.getEffectType())) {
            if (this.effectImmunities.get(effect.getEffectType()) >= this.age) {
                return false;
            }
            else
                this.effectImmunities.remove(effect.getEffectType());
        }
        return original.call(effect);
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder addStaffAttributes(DefaultAttributeContainer.Builder original) {
        for (EntityAttribute attribute : ModAttributes.GLOBALS)
            original.add(attribute);
        return original;
    }
}
