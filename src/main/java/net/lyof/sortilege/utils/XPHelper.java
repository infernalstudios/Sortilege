package net.lyof.sortilege.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.HashMap;
import java.util.Map;

public class XPHelper {
    public static Map<String, Integer> XP_SAVES = new HashMap<>();


    public static int getTotalxp(Player player, ServerLevel server) {
        return getTotalxp(player.experienceLevel, player.experienceProgress, server);
    }

    public static int getTotalxp(int level, float progress, ServerLevel server) {
        Player dummy = FakePlayerFactory.getMinecraft(server);
        int total = 0;

        for (int i = 0; i <= level; i++) {
            dummy.experienceLevel = i;
            total += dummy.getXpNeededForNextLevel() * (i == level ? progress : 1);
        }

        return total;
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
