package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.utils.MathHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
        if (ModJsonConfigs.get("enchantments.increased_costs", false)) {
            if (levelcost == 1) levelcost = 5;
            else if (levelcost == 2) levelcost = 15;
            else if (levelcost == 3) levelcost = 30;
        }

        this.giveExperienceLevels(-levelcost);
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }

        this.enchantmentSeed = MathHelper.rnd.nextInt();
    }
}
