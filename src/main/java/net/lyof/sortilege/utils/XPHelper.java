package net.lyof.sortilege.utils;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class XPHelper {
    public static Map<String, Integer> XP_SAVES = new HashMap<>();
    private static final Map<Integer, Integer> totalxpCache = new HashMap<>();


    public static int getTotalxp(PlayerEntity player, ServerWorld server) {
        return getTotalxp(player.experienceLevel, player.experienceProgress, server);
    }

    public static int getTotalxp(int level, float progress, ServerWorld server) {
        PlayerEntity dummy = FakePlayer.get(server);
        int total = 0;

        if (totalxpCache.containsKey(level)) {
            dummy.experienceLevel = level;
            return (int) (totalxpCache.get(level) + dummy.getNextLevelExperience() * progress);
        }

        for (int i = 0; i <= level; i++) {
            dummy.experienceLevel = i;
            total += dummy.getNextLevelExperience() * (i == level ? progress : 1);
        }

        totalxpCache.put(level, total);
        return total;
    }

    public static void dropxpPinata(World world, double x, double y, double z, int amount) {
        for (int i = 0; i < amount; i++) {
            world.spawnEntity(new ExperienceOrbEntity(
                    world,
                    x,
                    y,
                    z,
                    1));
        }
    }
}
