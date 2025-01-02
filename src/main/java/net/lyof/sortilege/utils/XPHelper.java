package net.lyof.sortilege.utils;

import net.lyof.sortilege.capabilities.entity.EntityXpStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.HashMap;
import java.util.Map;

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


    /* DEPRECATED MEMBERS KEPT FOR BIN COMPAT */

    // DO NOT USE!!! Use one of the above methods instead that uses the EntityXpStorage capability instead
    @Deprecated(forRemoval = true)
    public static final Map<String, Integer> XP_SAVES = new HashMap<>();

    // DO NOT USE!!! Use getTotalxp(Player) instead
    @Deprecated(forRemoval = true)
    public static int getTotalxp(int level, float progress, ServerLevel server) {
        var player = FakePlayerFactory.getMinecraft(server);
        player.experienceLevel = level;
        player.experienceProgress = progress;
        return getTotalxp(player);
    }

    @Deprecated(forRemoval = true)
    public static void dropxpPinata(Level world, double x, double y, double z, int amount) {
        // server will handle sending the xp to client
        if (world.isClientSide) return;

        ExperienceOrb.award((ServerLevel) world, new Vec3(x, y, z), amount);
    }
}
