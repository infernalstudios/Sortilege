package net.lyof.sortilege.util;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class XPHelper {
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

    public static int getTotalXP(PlayerEntity player, ServerWorld server) {
        return getTotalXP(player.experienceLevel, player.experienceProgress, server);
    }

    public static int getTotalXP(int level, float progress, ServerWorld server) {
        PlayerEntity dummy = FakePlayer.get(server);
        int total = 0;

        for (int i = 0; i <= level; i++) {
            dummy.experienceLevel = i;
            total += dummy.getNextLevelExperience() * (i == level ? progress : 1);
        }

        return total;
    }
}
