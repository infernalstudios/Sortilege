package net.lyof.sortilege.enchant.custom;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DodgeChanceEnchant {
    public static float get(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel server))
            return 0;

        List<Float> values = new ArrayList<>();

        ItemStack stack;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stack = entity.getItemBySlot(slot);
            if (stack.isEmpty())
                continue;

            for (Object2IntMap.Entry<Holder<Enchantment>> enchant : stack.getEnchantments().entrySet()) {
                if (!enchant.getKey().value().isSupportedItem(stack))
                    continue;
                if (!enchant.getKey().value().matchingSlot(slot))
                    continue;

                LootContext context = new LootContext.Builder(new LootParams.Builder(server)
                        .withParameter(LootContextParams.THIS_ENTITY, entity)
                        .withParameter(LootContextParams.ENCHANTMENT_LEVEL, enchant.getIntValue())
                        .withParameter(LootContextParams.ORIGIN, entity.position())
                        .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                        .create(LootContextParamSets.ENCHANTED_DAMAGE)).create(Optional.empty());

                for (ConditionalEffect<EnchantmentValueEffect> effect : enchant.getKey().value().getEffects(ModEnchants.DODGE_CHANCE)) {
                    if (effect.matches(context))
                        values.add(effect.effect().process(enchant.getIntValue(), entity.getRandom(), 0));
                }
            }
        }
        return values.isEmpty() ? 0 : Collections.max(values);
    }
}
