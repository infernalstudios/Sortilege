package net.lyof.sortilege.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public class MathHelper {
    public static final Random rnd = new Random();

    public static Vec3d getLookVector(PlayerEntity player) {
        double y = Math.sin(-player.getPitch() * Math.PI / 180);
        double nullifier = Math.cos(player.getPitch() * Math.PI / 180);

        double x = Math.sin(-player.getYaw() * Math.PI / 180) * nullifier;
        double z = Math.cos( player.getYaw() * Math.PI / 180) * nullifier;

        return new Vec3d(x, y, z);
    }

    public static int randint(int max) {
        return randint(0, max + 1);
    }

    public static int randint(int min, int max) {
        return rnd.nextInt(max - min) + min;
    }

    public static <T> T randi(List<T> list) {
        if (list.size() == 1) return list.get(0);
        if (list.size() == 0) return null;
        return list.get(randint(list.size() - 1));
    }

    public static int toInt(Object x) {
        return (int) Math.round(Double.parseDouble(String.valueOf(x)));
    }
}
