package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.lyof.sortilege.util.ItemHelper;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements EnchantLearner {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public int experienceLevel;
    @Shadow public float experienceProgress;
    @Shadow public abstract boolean isCreative();
    @Shadow public abstract void addExperience(int experience);

    @WrapMethod(method = "applyEnchantmentCosts")
    public void applyEnchantmentCosts(ItemStack enchantedItem, int levelcost, Operation<Void> original) {
        if (levelcost > 0 && ConfigEntries.doIncreasedEnchantCosts && ConfigEntries.increasedEnchantCosts.size() == 3)
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
            int safe_xp = (int) Math.round(XPHelper.getTotalXP(this.experienceLevel, this.experienceProgress, world) * ConfigEntries.selfXPRatio);
            int drop_xp = (int) Math.round(XPHelper.getTotalXP(this.experienceLevel, this.experienceProgress, world) * ConfigEntries.dropXPRatio);

            this.experienceLevel = 0;
            this.experienceProgress = 0f;
            this.addExperience(safe_xp);

            cir.setReturnValue(drop_xp);
        }
    }

    @Inject(method = "damageShield", at = @At("HEAD"))
    public void damageLapisShield(float amount, CallbackInfo ci) {
        ItemStack stack = this.getOffHandStack();
        if (!ConfigEntries.lapisShieldEnabled || !stack.isOf(ModItems.LAPIS_SHIELD)) return;

        PlayerEntity self = (PlayerEntity) (Object) this;

        LapisShieldItem.onSuccessfulUse(stack, self, amount);
        if (!this.getWorld().isClient())
            self.incrementStat(Stats.USED.getOrCreateStat(ModItems.LAPIS_SHIELD));
    }

    @WrapMethod(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;")
    private ItemEntity preventStorytoldDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, Operation<ItemEntity> original) {
        if (!throwRandomly && ItemHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, stack))
            return null;
        return original.call(stack, throwRandomly, retainOwnership);
    }


    @Unique private EnchantKnowledge sorti_knowledge;

    @Override
    public EnchantKnowledge sorti_getKnowledge() {
        return this.sorti_knowledge;
    }

    @Override
    public void sorti_setKnowledge(EnchantKnowledge knowledge) {
        this.sorti_knowledge = knowledge;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustom(NbtCompound nbt, CallbackInfo ci) {
        if (this.sorti_knowledge != null)
            nbt.put(EnchantKnowledge.PLAYER_KEY, this.sorti_knowledge.write(new NbtCompound()));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustom(NbtCompound nbt, CallbackInfo ci) {
        this.sorti_knowledge = EnchantKnowledge.read(nbt, (PlayerEntity) (Object) this);
    }
}
