package net.lyof.sortilege.utils;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.capabilities.entity.EntityXpStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.HashMap;
import java.util.Map;

public class XPHelper {
    private static final Map<Integer, Integer> totalxpCache = new HashMap<>();


    public static boolean hasXP(Player player, int amount) {
        float count = player.getXpNeededForNextLevel() * player.experienceProgress;
        int i = 0;
        while (count < amount) {
            if (player.experienceLevel <= 0) {
                player.experienceLevel += i;
                return false;
            }

            i++;
            player.experienceLevel -= 1;
            count += player.getXpNeededForNextLevel();
        }
        player.experienceLevel += i;
        return true;
    }

    public static int getTotalxp(Player player, ServerLevel server) {
        return getTotalxp(player.experienceLevel, player.experienceProgress, server);
    }

    public static int getTotalxp(int level, float progress, ServerLevel server) {
        Player dummy = FakePlayerFactory.getMinecraft(server);
        int total = 0;

        if (totalxpCache.containsKey(level)) {
            dummy.experienceLevel = level;
            return (int) (totalxpCache.get(level) + dummy.getXpNeededForNextLevel() * progress);
        }

        for (int i = 0; i <= level; i++) {
            dummy.experienceLevel = i;
            total += (int) (dummy.getXpNeededForNextLevel() * (i == level ? progress : 1));
        }

        totalxpCache.put(level, total);
        return total;
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

    public static void dropxpPinata(Level world, double x, double y, double z, int amount) {
        for (int i = 0; i < amount; i++) {
            world.addFreshEntity(new ExperienceOrb(
                    world,
                    x,
                    y,
                    z,
                    1));
        }
    }
}
