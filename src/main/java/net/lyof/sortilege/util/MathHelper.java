package net.lyof.sortilege.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class MathHelper {
    public static final Random rnd = new Random();

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

    public static <T> T randi(List<T> list, RandomSource rnd) {
        if (list.size() == 1) return list.get(0);
        if (list.isEmpty()) return null;
        return list.get(rnd.nextInt(list.size()));
    }

    public static RandomSource getRandom(Level world) {
        long t = world.getDayTime();
        if (world.isClientSide()) t += 1;

        return RandomSource.create(t);
    }
}
