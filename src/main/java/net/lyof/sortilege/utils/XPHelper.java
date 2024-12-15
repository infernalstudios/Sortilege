package net.lyof.sortilege.utils;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class XPHelper {
    public static Map<String, Integer> XP_SAVES = new HashMap<>();

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
}
