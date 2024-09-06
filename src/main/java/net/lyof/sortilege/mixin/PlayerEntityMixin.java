package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    /**
     * @author Lyof - Sortilege
     * @reason Enchant XP Costs Change
     */
    @Overwrite
    public void applyEnchantmentCosts(ItemStack itemstack, int levelcost) {
        if (ConfigEntries.doIncreasedEnchantCosts && ConfigEntries.increasedEnchantCosts.size() == 3)
            levelcost = (int) Math.round(ConfigEntries.increasedEnchantCosts.get(levelcost - 1));

        this.experienceLevel -= levelcost;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }

        this.enchantmentTableSeed = MathHelper.rnd.nextInt();
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

    @Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean keepInventory(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        if (rule == GameRules.KEEP_INVENTORY && this.isCreative())
            return true;
        return instance.getBoolean(rule);
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
}
