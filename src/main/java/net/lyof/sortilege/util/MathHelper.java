package net.lyof.sortilege.util;

import net.lyof.sortilege.Sortilege;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static <T> T randi(List<T> list, net.minecraft.util.math.random.Random rnd) {
        if (list.size() == 1) return list.get(0);
        if (list.size() == 0) return null;
        return list.get(rnd.nextInt(list.size()));
    }

    public static int toInt(Object x) {
        return (int) Math.round(Double.parseDouble(String.valueOf(x)));
    }


    private static Map<Integer, Formatting> COLOR_CACHE = new HashMap<>();

    public static Formatting getClosestFormatting(float[] rgb) {
        int color = net.minecraft.util.math.MathHelper.packRgb(rgb[0], rgb[1], rgb[2]);
        if (COLOR_CACHE.containsKey(color)) return COLOR_CACHE.get(color);

        int distance = -1;
        Formatting result = Formatting.BLACK;
        for (Formatting formatting : Formatting.values()) {
            if (formatting.isColor())
                Sortilege.log(Math.abs(formatting.getColorValue() - color) + " " + distance + " " + formatting.getName());
            if (formatting.isColor() && (Math.abs(formatting.getColorValue() - color) < distance || distance < 0)) {
                distance = Math.abs(formatting.getColorValue() - color);
                result = formatting;
            }
        }

        COLOR_CACHE.putIfAbsent(color, result);
        return result;
    }
}
