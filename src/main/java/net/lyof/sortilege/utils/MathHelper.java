package net.lyof.sortilege.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class MathHelper {
    public static final Random rnd = new Random();

    public static Vec3 getLookVector(Player player) {
        double y = Math.sin(-player.xRotO * Math.PI / 180);
        double nullifier = Math.cos(player.xRotO * Math.PI / 180);

        double x = Math.sin(-player.yRotO * Math.PI / 180) * nullifier;
        double z = Math.cos( player.yRotO * Math.PI / 180) * nullifier;

        return new Vec3(x, y, z);
    }

    public static int randint(int max) {
        return randint(0, max);
    }

    public static int randint(int min, int max) {
        return rnd.nextInt(max - min) + min;
    }

    public static <T> T randi(List<T> list) {
        if (list.size() == 1) return list.get(0);
        return list.get(randint(list.size() - 1));
    }
}
