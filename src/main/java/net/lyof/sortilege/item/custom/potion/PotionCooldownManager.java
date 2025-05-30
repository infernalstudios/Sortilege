package net.lyof.sortilege.item.custom.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class PotionCooldownManager {
    private static final Map<Boolean, Map<LivingEntity, Map<String, Pair<Integer, Integer>>>> COOLDOWNS = Map.of(
            true, new HashMap<>(),
            false, new HashMap<>());

    public static void set(ItemStack stack, LivingEntity user, int length) {
        COOLDOWNS.get(user.getWorld().isClient()).putIfAbsent(user, new HashMap<>());
        COOLDOWNS.get(user.getWorld().isClient()).get(user)
                .putIfAbsent(CustomPotionData.getPotionItemType(stack), new Pair<>(user.age, user.age + length));
    }

    public static float getProgress(ItemStack stack, LivingEntity user) {
        String key = CustomPotionData.getPotionItemType(stack);

        if (!COOLDOWNS.get(user.getWorld().isClient()).containsKey(user)
                || !COOLDOWNS.get(user.getWorld().isClient()).get(user).containsKey(key)) return 0;

        Pair<Integer, Integer> time = COOLDOWNS.get(user.getWorld().isClient()).get(user).get(key);
        if (time.getRight() < user.age) {
            COOLDOWNS.get(true).get(user).remove(key);
            COOLDOWNS.get(false).get(user).remove(key);
            return 0;
        }
        return 1 - (user.age - time.getLeft()) / (float ) (time.getRight() - time.getLeft());
    }

    public static void clear() {
        COOLDOWNS.get(true).clear();
        COOLDOWNS.get(false).clear();
    }

    public static void clear(LivingEntity user) {
        COOLDOWNS.get(true).remove(user);
        COOLDOWNS.get(false).remove(user);
    }
}
