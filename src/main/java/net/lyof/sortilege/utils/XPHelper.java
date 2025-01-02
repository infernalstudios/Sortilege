package net.lyof.sortilege.utils;

import net.lyof.sortilege.capabilities.entity.EntityXpStorage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class XPHelper {
    public static boolean hasXP(Player player, int amount) {
        return getTotalxp(player) >= amount;
    }

    public static int getTotalxp(Player player) {
        int level = player.experienceLevel;
        float progress = player.experienceProgress;
        float total = 0;

        for (int i = 0; i <= level; i++) {
            player.experienceLevel = i;
            total += player.getXpNeededForNextLevel() * (i == level ? progress : 1.0F);
        }

        // reset the player's level to what we started with
        player.experienceLevel = level;
        return Math.round(total);
    }

    public static int getSavedXp(LivingEntity entity) {
        return EntityXpStorage.getIfPresent(entity, EntityXpStorage::getXp, () -> 0);
    }

    public static void removeSavedXp(LivingEntity entity) {
        EntityXpStorage.ifPresent(entity, storage -> storage.setXp(0));
    }

    public static void setSavedXp(LivingEntity entity, int xp) {
        EntityXpStorage.ifPresent(entity, storage -> storage.setXp(xp));
    }

    public static void setSavedXp(LivingEntity entity, int xp, boolean onlyIfZero) {
        EntityXpStorage.ifPresent(entity, storage -> storage.setXp(xp, onlyIfZero));
    }

}
