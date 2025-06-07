package net.lyof.sortilege.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public class MathHelper {
    public static final Random rnd = new Random();

    public static Vec3d getLookVector(LivingEntity entity) {
        double y = Math.sin(-entity.getPitch() * Math.PI / 180);
        double nullifier = Math.cos(entity.getPitch() * Math.PI / 180);

        double x = Math.sin(-entity.getYaw() * Math.PI / 180) * nullifier;
        double z = Math.cos( entity.getYaw() * Math.PI / 180) * nullifier;

        return new Vec3d(x, y, z);
    }

    public static int randint(int max) {
        return randint(0, max + 1);
    }

    public static int randint(int min, int max) {
        return rnd.nextInt(max - min) + min;
    }

    public static <T> T randi(List<T> list) {
        return randi(list, rnd);
    }

    public static <T> T randi(List<T> list, Random random) {
        if (list.size() == 1) return list.get(0);
        if (list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    public static <T> T randi(List<T> list, net.minecraft.util.math.random.Random rnd) {
        if (list.size() == 1) return list.get(0);
        if (list.isEmpty()) return null;
        return list.get(rnd.nextInt(list.size()));
    }

    public static int toInt(Object x) {
        return (int) Math.round(Double.parseDouble(String.valueOf(x)));
    }
}
