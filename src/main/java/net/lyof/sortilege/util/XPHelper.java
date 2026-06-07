package net.lyof.sortilege.util;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class XPHelper {
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

    public static int getTotalXP(Player player, ServerLevel server) {
        return getTotalXP(player.experienceLevel, player.experienceProgress, server);
    }

    public static int getTotalXP(int level, float progress, ServerLevel server) {
        Player dummy = FakePlayer.get(server);
        int total = 0;

        for (int i = 0; i <= level; i++) {
            dummy.experienceLevel = i;
            total += dummy.getXpNeededForNextLevel() * (i == level ? progress : 1);
        }

        return total;
    }
}
