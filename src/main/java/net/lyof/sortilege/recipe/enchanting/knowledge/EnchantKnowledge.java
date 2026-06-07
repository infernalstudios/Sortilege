package net.lyof.sortilege.recipe.enchanting.knowledge;

import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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
        if (stack.is(Items.ENCHANTED_BOOK) || !stack.hasTag() || !stack.getTag().getBoolean(LEARNABLE_KEY)) return false;
        return this.getKnownLevel(enchant) < value;
    }

    public void learn(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : stack.getItem() instanceof KnowledgeBookItem ?
                KnowledgeBookItem.getKnowledge(stack).getEntries() : EnchantmentHelper.getEnchantments(stack).entrySet())
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
    public static final String AUTHORS_KEY = "sorti_Authors";
    public static final String LEARNABLE_KEY = "sorti_IsLearnable";

    public CompoundTag write(CompoundTag nbt) {
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            nbt.putInt(BuiltInRegistries.ENCHANTMENT.getKey(entry.getKey()).toString(), entry.getValue());
        return nbt;
    }

    public static EnchantKnowledge read(CompoundTag nbt) {
        EnchantKnowledge self = new EnchantKnowledge();
        if (!nbt.contains(KNOWLEDGE_KEY, Tag.TAG_COMPOUND)) return self;

        nbt = nbt.getCompound(KNOWLEDGE_KEY);
        for (String enchant : nbt.getAllKeys())
            self.learn(BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(enchant)), nbt.getInt(enchant));

        return self;
    }

    @Override
    public String toString() {
        return "EnchantKnowledge{" +
                known.entrySet().stream().map(entry -> entry.getKey().getDescriptionId() + " " + entry.getValue()).toList() +
                '}';
    }
}
