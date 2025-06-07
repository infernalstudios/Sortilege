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
import net.lyof.sortilege.recipe.brewing.custom.MixBrewingRecipe;
import net.lyof.sortilege.recipe.emi.BetterBrewingEmiRecipe;
import net.lyof.sortilege.recipe.emi.CatalystEmiRecipe;
import net.lyof.sortilege.recipe.emi.CauldronBrewingEmiRecipe;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.recipe.enchanting.EnchantingCatalyst;
import net.lyof.sortilege.recipe.smithing.LimitBreakRecipe;
import net.lyof.sortilege.recipe.smithing.SoulbindingRecipe;
import net.lyof.sortilege.setup.ModTags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {
    public static final EmiRecipeCategory ENCHANTING_CATEGORY = new EmiRecipeCategory(Sortilege.makeID("enchanting"),
            EmiStack.of(Items.ENCHANTING_TABLE),
            new EmiTexture(Sortilege.makeID("textures/gui/emi/enchanting.png"), 0, 0, 16, 16));

    public static final EmiRecipeCategory CAULDRON_CATEGORY = new EmiRecipeCategory(Sortilege.makeID("cauldron_brewing"),
            EmiStack.of(Items.CAULDRON),
            new EmiTexture(Sortilege.makeID("textures/gui/emi/cauldron_brewing.png"), 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ENCHANTING_CATEGORY);
        registry.addWorkstation(ENCHANTING_CATEGORY, EmiStack.of(Items.ENCHANTING_TABLE));
        registry.addCategory(CAULDRON_CATEGORY);
        registry.addWorkstation(CAULDRON_CATEGORY, EmiStack.of(Items.CAULDRON));

        registry.addRecipe(new SpecialSmithingEmiRecipe(new SoulbindingRecipe(Sortilege.makeID("soulbinding_instance")),
                EmiIngredient.of(ModTags.Items.SOULBINDERS)));
        registry.addRecipe(new SpecialSmithingEmiRecipe(new LimitBreakRecipe(Sortilege.makeID("limit_break_instance")),
                EmiIngredient.of(ModTags.Items.LIMIT_BREAKER)));

        for (Map.Entry<Item, List<Enchantment>> entry : EnchantingCatalyst.CATALYSTS.entrySet())
            registry.addRecipe(new CatalystEmiRecipe(entry.getKey(), entry.getValue()));

        for (BrewingRecipe recipe : registry.getRecipeManager().listAllOfType(ModRecipeTypes.BREWING))
            registry.addRecipe(new BetterBrewingEmiRecipe(recipe));

        for (CauldronBrewingRecipe recipe : registry.getRecipeManager().listAllOfType(ModRecipeTypes.CAULDRON_BREWING))
            registry.addRecipe(new CauldronBrewingEmiRecipe(recipe));
    }
}
