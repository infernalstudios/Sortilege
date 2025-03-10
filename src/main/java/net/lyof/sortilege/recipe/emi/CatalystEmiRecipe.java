package net.lyof.sortilege.recipe.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.EmiCompat;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;

public class CatalystEmiRecipe extends BasicEmiRecipe {
    protected final EmiStack item;
    protected final List<EmiStack> enchantedBooks;

    public CatalystEmiRecipe(Item item, List<Enchantment> enchants) {
        super(EmiCompat.ENCHANTING_CATEGORY, Sortilege.makeID("/enchanting/catalyst/" + Registries.ITEM.getId(item).getPath()),
                120, 18);

        this.item = EmiStack.of(item);
        this.enchantedBooks = enchants.stream().map(enchant ->
                EmiStack.of(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchant, 1)))).toList();

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
