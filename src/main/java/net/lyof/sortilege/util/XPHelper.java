package net.lyof.sortilege.util;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class XPHelper {
    public static Map<String, Integer> XP_SAVES = new HashMap<>();

    public static final TrackedData<Integer> BOUNTY = new TrackedData<>(41, TrackedDataHandlerRegistry.INTEGER);


    public static boolean hasXP(PlayerEntity player, int amount) {
        float count = player.getNextLevelExperience() * player.experienceProgress;
        int i = 0;
        while (count < amount) {
            if (player.experienceLevel <= 0) {
                player.experienceLevel += i;
                return false;
            }

            i++;
            player.experienceLevel -= 1;
            count += player.getNextLevelExperience();
        }
        player.experienceLevel += i;
        return true;
    }

    public static int getTotalxp(PlayerEntity player, ServerWorld server) {
        return getTotalxp(player.experienceLevel, player.experienceProgress, server);
    }

    public static int getTotalxp(int level, float progress, ServerWorld server) {
        PlayerEntity dummy = FakePlayer.get(server);
        int total = 0;

        for (int i = 0; i <= level; i++) {
            dummy.experienceLevel = i;
            total += dummy.getNextLevelExperience() * (i == level ? progress : 1);
        }

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
