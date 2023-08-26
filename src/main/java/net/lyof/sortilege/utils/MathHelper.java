package net.lyof.sortilege.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class MathHelper {
    public static Vec3 getLookVector(Player player) {
        double y = Math.sin(-player.xRotO * Math.PI / 180);
        double nullifier = Math.cos(player.xRotO * Math.PI / 180);

        double x = Math.sin(-player.yRotO * Math.PI / 180) * nullifier;
        double z = Math.cos( player.yRotO * Math.PI / 180) * nullifier;

        return new Vec3(x, y, z);
    }
}
