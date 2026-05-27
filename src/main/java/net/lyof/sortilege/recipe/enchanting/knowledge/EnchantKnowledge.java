package net.lyof.sortilege.recipe.enchanting.knowledge;

import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnchantKnowledge {
    protected final Map<Enchantment, Integer> known;

    public EnchantKnowledge() {
        this.known = new HashMap<>();
    }

    public Set<Map.Entry<Enchantment, Integer>> getEntries() {
        return this.known.entrySet();
    }

    public boolean isLearnable(ItemStack stack, Enchantment enchant, int value) {
        if (stack.isOf(Items.ENCHANTED_BOOK) || !stack.hasNbt() || !stack.getNbt().getBoolean(LEARNABLE_KEY)) return false;
        return this.getKnownLevel(enchant) < value;
    }

    public void learn(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : stack.getItem() instanceof KnowledgeBookItem ?
                KnowledgeBookItem.getKnowledge(stack).getEntries() : EnchantmentHelper.get(stack).entrySet())
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

    public static final String KNOWLEDGE_KEY = "sorti_EnchantKnowledge";
    public static final String LEARNABLE_KEY = "sorti_IsLearnable";

    public NbtCompound write(NbtCompound nbt) {
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            nbt.putInt(Registries.ENCHANTMENT.getId(entry.getKey()).toString(), entry.getValue());
        return nbt;
    }

    public static EnchantKnowledge read(NbtCompound nbt) {
        EnchantKnowledge self = new EnchantKnowledge();
        if (!nbt.contains(KNOWLEDGE_KEY, NbtElement.COMPOUND_TYPE)) return self;

        nbt = nbt.getCompound(KNOWLEDGE_KEY);
        for (String enchant : nbt.getKeys())
            self.learn(Registries.ENCHANTMENT.get(new Identifier(enchant)), nbt.getInt(enchant));

        return self;
    }

    @Override
    public String toString() {
        return "EnchantKnowledge{" +
                known.entrySet().stream().map(entry -> entry.getKey().getTranslationKey() + " " + entry.getValue()).toList() +
                '}';
    }
}
