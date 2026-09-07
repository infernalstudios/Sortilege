package net.lyof.sortilege.recipe;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.lyof.sortilege.recipe.emi.BetterBrewingEmiRecipe;
import net.lyof.sortilege.recipe.emi.CatalystEmiRecipe;
import net.lyof.sortilege.recipe.emi.CauldronBrewingEmiRecipe;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.recipe.enchanting.catalyst.CatalystRecipe;
import net.lyof.sortilege.recipe.smithing.LimitBreakRecipe;
import net.lyof.sortilege.recipe.smithing.SoulbindingRecipe;
import net.lyof.sortilege.setup.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {
    public static final EmiRecipeCategory ENCHANTING_CATEGORY = new EmiRecipeCategory(Sortilege.MOD.makeID("enchanting"),
            EmiStack.of(Items.ENCHANTING_TABLE),
            new EmiTexture(Sortilege.MOD.makeID("textures/gui/emi/enchanting.png"), 0, 0, 16, 16));

    public static final EmiRecipeCategory CAULDRON_CATEGORY = new EmiRecipeCategory(Sortilege.MOD.makeID("cauldron_brewing"),
            EmiStack.of(Items.CAULDRON),
            new EmiTexture(Sortilege.MOD.makeID("textures/gui/emi/cauldron_brewing.png"), 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ENCHANTING_CATEGORY);
        registry.addWorkstation(ENCHANTING_CATEGORY, EmiStack.of(Items.ENCHANTING_TABLE));
        registry.addCategory(CAULDRON_CATEGORY);
        registry.addWorkstation(CAULDRON_CATEGORY, EmiStack.of(Items.CAULDRON));

        registry.addRecipe(new SpecialSmithingEmiRecipe(new SoulbindingRecipe(), Sortilege.MOD.makeID("soulbinding_instance"),
                EmiIngredient.of(ModTags.Items.SOULBINDERS)));
        registry.addRecipe(new SpecialSmithingEmiRecipe(new LimitBreakRecipe(), Sortilege.MOD.makeID("limit_break_instance"),
                EmiIngredient.of(ModTags.Items.LIMIT_BREAKER)));

        Map<Item, List<Holder<Enchantment>>> catalysts = new HashMap<>();
        for (RecipeHolder<CatalystRecipe> recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CATALYST))
            catalysts.merge(recipe.value().item(), recipe.value().enchants(), (old, self) -> {
                Set<Holder<Enchantment>> enchants = new HashSet<>();
                enchants.addAll(old);
                enchants.addAll(self);
                return new ArrayList<>(enchants);
            });
        for (Map.Entry<Item, List<Holder<Enchantment>>> entry : catalysts.entrySet())
            registry.addRecipe(new CatalystEmiRecipe(entry.getKey(), entry.getValue()));

        for (RecipeHolder<BrewingRecipe> recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BREWING))
            registry.addRecipe(new BetterBrewingEmiRecipe(recipe));

        for (RecipeHolder<CauldronBrewingRecipe> recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CAULDRON_BREWING))
            registry.addRecipe(new CauldronBrewingEmiRecipe(recipe));
    }
}
