package net.lyof.sortilege.recipe.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.EmiCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public class CatalystEmiRecipe extends BasicEmiRecipe {
    protected final EmiStack item;
    protected final List<EmiStack> enchantedBooks;

    public CatalystEmiRecipe(Item item, List<Holder<Enchantment>> enchants) {
        super(EmiCompat.ENCHANTING_CATEGORY, Sortilege.MOD.makeID("/enchanting/catalyst/" + BuiltInRegistries.ITEM.getKey(item).getPath()),
                120, 18);

        this.item = EmiStack.of(item);
        this.enchantedBooks = enchants.stream().map(enchant ->
                EmiStack.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, 1)))).toList();

        this.inputs.add(this.item);
        this.outputs.add(EmiStack.of(Items.ENCHANTED_BOOK));
    }

    @Override
    public int getDisplayHeight() {
        return 18 + ((this.enchantedBooks.size()-1) / 4) * 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(this.item, 6, 0);

        for (int i = 0; i < this.enchantedBooks.size(); i++) {
            widgets.addSlot(this.enchantedBooks.get(i), 42 + (i % 4)*18, (i / 4)*18);
        }
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
