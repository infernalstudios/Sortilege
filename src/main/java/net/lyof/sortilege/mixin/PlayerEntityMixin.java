package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @Shadow public int experienceLevel;
    @Shadow public float experienceProgress;
    @Shadow public int totalExperience;
    @Shadow protected int enchantmentTableSeed;
    @Shadow public abstract boolean isCreative();
    @Shadow public abstract void addExperience(int experience);

    @WrapMethod(method = "applyEnchantmentCosts")
    public void applyEnchantmentCosts(ItemStack enchantedItem, int levelcost, Operation<Void> original) {
        if (ConfigEntries.doIncreasedEnchantCosts && ConfigEntries.increasedEnchantCosts.size() == 3)
            levelcost = (int) Math.round(ConfigEntries.increasedEnchantCosts.get(levelcost - 1));

        original.call(enchantedItem, levelcost);
    }

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    public void linearXpScaling(CallbackInfoReturnable<Integer> cir) {
        if (ConfigEntries.xpLinearCost > 0)
            cir.setReturnValue(ConfigEntries.xpLinearCost);
    }

    @Inject(method = "addExperienceLevels", at = @At("TAIL"))
    public void xpCapLevel(int levels, CallbackInfo ci) {
        if (ConfigEntries.xpLevelCap > -1 && this.experienceLevel > ConfigEntries.xpLevelCap) {
            this.experienceLevel = ConfigEntries.xpLevelCap;
            this.experienceProgress = 0f;
        }
    }

    @Inject(method = "addExperience", at = @At("TAIL"))
    public void xpCap(int experience, CallbackInfo ci) {
        if (ConfigEntries.xpLevelCap > -1 && this.experienceLevel >= ConfigEntries.xpLevelCap) {
            this.experienceLevel = ConfigEntries.xpLevelCap;
            this.experienceProgress = 0f;
        }
    }

    @WrapOperation(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean keepInventory(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule, Operation<Boolean> original) {
        if (rule == GameRules.KEEP_INVENTORY && this.isCreative())
            return true;
        return original.call(instance, rule);
    }

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    public void keepXP(CallbackInfoReturnable<Integer> cir) {
        if (ConfigEntries.doXPKeep && this.getWorld() instanceof ServerWorld world) {
            int safe_xp = (int) Math.round(XPHelper.getTotalxp(this.experienceLevel, this.experienceProgress, world) * ConfigEntries.selfXPRatio);
            int drop_xp = (int) Math.round(XPHelper.getTotalxp(this.experienceLevel, this.experienceProgress, world) * ConfigEntries.dropXPRatio);

            this.experienceLevel = 0;
            this.experienceProgress = 0f;
            this.addExperience(safe_xp);

            cir.setReturnValue(drop_xp);
        }
    }

    @Inject(method = "damageShield", at = @At("HEAD"))
    public void damageLapisShield(float amount, CallbackInfo ci) {
        ItemStack stack = this.getOffHandStack();
        if (!stack.isOf(ModItems.LAPIS_SHIELD)) return;

        PlayerEntity self = (PlayerEntity) (Object) this;

        LapisShieldItem.putOnCooldown(stack, self);
        ModParticles.spawnWisps(self.getWorld(), this.getX(), this.getY() + this.getEyeHeight(this.getPose()) / 2, this.getZ(),
                16, new float[]{0.3f, 0.3f, 1f});

        if (!this.getWorld().isClient())
            self.incrementStat(Stats.USED.getOrCreateStat(ModItems.LAPIS_SHIELD));

        if (amount >= 3.0F) {
            stack.damage(1, self, e -> e.sendToolBreakStatus(Hand.OFF_HAND));
            if (stack.isEmpty())
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.getWorld().random.nextFloat() * 0.4F);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickLapisShield(CallbackInfo ci) {
        ItemStack stack = this.getOffHandStack();
        if (!stack.isOf(ModItems.LAPIS_SHIELD)) return;

        if (LapisShieldItem.getCooldownEnd(stack) <= this.getWorld().getTime()
                || LapisShieldItem.getCooldownEnd(stack) - ConfigEntries.lapisShieldCooldown - 1 > this.getWorld().getTime())
            LapisShieldItem.removeCooldown(stack);
    }
}
