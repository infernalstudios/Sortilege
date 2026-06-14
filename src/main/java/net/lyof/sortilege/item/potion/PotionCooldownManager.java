package net.lyof.sortilege.item.potion;

import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PotionCooldownManager {
    private static final Map<Boolean, Map<LivingEntity, Map<String, Tuple<Integer, Integer>>>> COOLDOWNS = Map.of(
            true, new HashMap<>(),
            false, new HashMap<>());

    public static void set(ItemStack stack, LivingEntity user, int length) {
        COOLDOWNS.get(user.level().isClientSide()).putIfAbsent(user, new HashMap<>());
        COOLDOWNS.get(user.level().isClientSide()).get(user)
                .putIfAbsent(PotionHelper.getPotionItemType(stack), new Tuple<>(user.tickCount, user.tickCount + length));
    }

    public static float getProgress(ItemStack stack, LivingEntity user, float tickDelta) {
        String key = PotionHelper.getPotionItemType(stack);

        if (!COOLDOWNS.get(user.level().isClientSide()).containsKey(user)
                || !COOLDOWNS.get(user.level().isClientSide()).get(user).containsKey(key)) return 0;

        Tuple<Integer, Integer> time = COOLDOWNS.get(user.level().isClientSide()).get(user).get(key);
        if (time.getB() < user.tickCount || time.getB() <= time.getA()) {
            if (COOLDOWNS.get(true).containsKey(user)) COOLDOWNS.get(true).get(user).remove(key);
            if (COOLDOWNS.get(false).containsKey(user)) COOLDOWNS.get(false).get(user).remove(key);
            return 0;
        }
        return (time.getB() - user.tickCount - tickDelta) / (float) (time.getB() - time.getA());
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
