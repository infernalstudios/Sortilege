package net.lyof.sortilege.mixin;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.MathHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public int experienceLevel;
    @Shadow public float experienceProgress;
    @Shadow public int totalExperience;

    @Shadow protected int enchantmentTableSeed;

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
}
