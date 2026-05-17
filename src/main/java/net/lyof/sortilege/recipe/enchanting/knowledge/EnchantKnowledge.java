package net.lyof.sortilege.recipe.enchanting.knowledge;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class EnchantKnowledge {
    protected Map<Enchantment, Integer> known;

    public EnchantKnowledge() {
        this.known = new HashMap<>();
    }

    public void learn(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(stack).entrySet())
            this.learn(entry.getKey(), entry.getValue());
    }

    public void learn(Enchantment enchant, int level) {
        if (enchant == null || level <= 0) return;

        int current = this.getKnownLevel(enchant);
        if (current == 0) this.known.put(enchant, level);
        else if (level > current) this.known.replace(enchant, level);
    }

    public boolean isKnown(Enchantment enchant) {
        return this.known.containsKey(enchant);
    }

    public int getKnownLevel(Enchantment enchant) {
        return this.known.getOrDefault(enchant, 0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("EnchantKnowledge{known=");
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            builder.append(Registries.ENCHANTMENT.getId(entry.getKey()).toString()).append(": ").append(entry.getValue()).append(", ");
        return builder.append("}").toString();
    }

    public static final String KEY = "sorti_EnchantKnowledge";

    public NbtCompound write(NbtCompound nbt) {
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            nbt.putInt(Registries.ENCHANTMENT.getId(entry.getKey()).toString(), entry.getValue());
        return nbt;
    }

    public static EnchantKnowledge read(NbtCompound nbt) {
        EnchantKnowledge self = new EnchantKnowledge();
        if (!nbt.contains(KEY, NbtElement.COMPOUND_TYPE)) return self;

        nbt = nbt.getCompound(KEY);
        for (String enchant : nbt.getKeys())
            self.learn(Registries.ENCHANTMENT.get(new Identifier(enchant)), nbt.getInt(enchant));

        return self;
    }
}
