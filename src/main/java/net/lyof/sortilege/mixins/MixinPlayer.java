package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.MathHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Shadow public abstract void giveExperienceLevels(int amount);
    @Shadow public int experienceLevel;
    @Shadow public float experienceProgress;
    @Shadow public int totalExperience;
    @Shadow protected int enchantmentSeed;

    /**
     * @author Lyof - Sortilege
     * @reason Enchant XP Costs Change
     */
    @Overwrite
    public void onEnchantmentPerformed(ItemStack itemstack, int levelcost) {
        if (ConfigEntries.DoIncreasedEnchantCosts && ConfigEntries.IncreasedEnchantCosts.size() == 3)
            levelcost = (int) Math.round(ConfigEntries.IncreasedEnchantCosts.get(levelcost - 1));

        this.giveExperienceLevels(-levelcost);
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }

        this.enchantmentSeed = MathHelper.rnd.nextInt();
    }

    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    public void linearXpScaling(CallbackInfoReturnable<Integer> cir) {
        if (ConfigEntries.xpLinearCost > 0)
            cir.setReturnValue(ConfigEntries.xpLinearCost);
    }
}
