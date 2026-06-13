package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements EnchantLearner {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow public int experienceLevel;
    @Shadow public float experienceProgress;
    @Shadow public abstract boolean isCreative();

    @Shadow public abstract void giveExperiencePoints(int xpPoints);

    @WrapMethod(method = "onEnchantmentPerformed")
    public void applyEnchantmentCosts(ItemStack enchantedItem, int levelcost, Operation<Void> original) {
        if (levelcost > 0 && ModConfig.doIncreasedEnchantCosts.get() && ModConfig.increasedEnchantCosts.get().size() == 3)
            levelcost = ModConfig.increasedEnchantCosts.get().get(levelcost - 1);

        original.call(enchantedItem, levelcost);
    }

    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    public void linearXpScaling(CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.xpLinearCost.get() > 0)
            cir.setReturnValue(ModConfig.xpLinearCost.get());
    }

    @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    public void xpCapLevel(int levels, CallbackInfo ci) {
        if (ModConfig.xpLevelCap.get() > -1 && this.experienceLevel > ModConfig.xpLevelCap.get()) {
            this.experienceLevel = ModConfig.xpLevelCap.get();
            this.experienceProgress = 0f;
        }
    }

    @Inject(method = "giveExperiencePoints", at = @At("TAIL"))
    public void xpCap(int experience, CallbackInfo ci) {
        if (ModConfig.xpLevelCap.get() > -1 && this.experienceLevel >= ModConfig.xpLevelCap.get()) {
            this.experienceLevel = ModConfig.xpLevelCap.get();
            this.experienceProgress = 0f;
        }
    }

    @WrapOperation(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    public boolean keepInventory(GameRules instance, GameRules.Key<GameRules.BooleanValue> rule, Operation<Boolean> original) {
        if (rule == GameRules.RULE_KEEPINVENTORY && this.isCreative())
            return true;
        return original.call(instance, rule);
    }

    @Inject(method = "getExperienceReward", at = @At("HEAD"), cancellable = true)
    public void keepXP(CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.doXPKeep.get() && this.level() instanceof ServerLevel world) {
            int safe_xp = (int) Math.round(XPHelper.getTotalXP(this.experienceLevel, this.experienceProgress, world) * ModConfig.selfXPRatio.get());
            int drop_xp = (int) Math.round(XPHelper.getTotalXP(this.experienceLevel, this.experienceProgress, world) * ModConfig.dropXPRatio.get());

            this.experienceLevel = 0;
            this.experienceProgress = 0f;
            this.giveExperiencePoints(safe_xp);

            cir.setReturnValue(drop_xp);
        }
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    public void damageLapisShield(float amount, CallbackInfo ci) {
        ItemStack stack = this.getOffhandItem();
        if (!ModConfig.lapisShieldEnabled.get() || !stack.is(ModItems.LAPIS_SHIELD)) return;

        Player self = (Player) (Object) this;

        LapisShieldItem.onSuccessfulUse(stack, self, amount);
        if (!this.level().isClientSide())
            self.awardStat(Stats.ITEM_USED.get(ModItems.LAPIS_SHIELD));
    }

    @WrapMethod(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;")
    private ItemEntity preventStorytoldDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, Operation<ItemEntity> original) {
        if (!throwRandomly && EnchantHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, stack))
            return null;
        return original.call(stack, throwRandomly, retainOwnership);
    }

    @Unique private ItemStack sorti_knowledgeCacher = null;
    @Unique private EnchantKnowledge sorti_knowledge = null;

    @Override
    public EnchantKnowledge sorti_getKnowledge(ItemStack cacher) {
        if (cacher != null && this.sorti_knowledgeCacher == cacher)
            return this.sorti_knowledge;

        EnchantKnowledge knowledge = new EnchantKnowledge();
        Player self = (Player) (Object) this;

        ItemStack stack;
        for (int i = 0; i < self.getInventory().getContainerSize(); i++) {
            stack = self.getInventory().getItem(i);
            if (stack.is(ModItems.KNOWLEDGE_BOOK) && KnowledgeBookItem.isAuthor(stack, self))
                knowledge.learn(stack);
        }
        this.sorti_knowledge = knowledge;
        this.sorti_knowledgeCacher = cacher;
        // TODO: Trinkets compat (by Sol)

        return this.sorti_knowledge;
    }
}
