package net.lyof.sortilege.setup;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.IBetterBrewingRecipe;
import net.lyof.sortilege.recipe.emi.BetterBrewingEmiRecipe;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.recipe.smithing.LimitBreakRecipe;
import net.lyof.sortilege.recipe.smithing.SoulbindingRecipe;

public class EmiCompat implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipe(new SpecialSmithingEmiRecipe(new SoulbindingRecipe(Sortilege.makeID("soulbinding_instance")),
                EmiIngredient.of(ModTags.Items.SOULBINDERS)));
        registry.addRecipe(new SpecialSmithingEmiRecipe(new LimitBreakRecipe(Sortilege.makeID("limit_break_instance")),
                EmiIngredient.of(ModTags.Items.LIMIT_BREAKER)));

        for (IBetterBrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            registry.addRecipe(new BetterBrewingEmiRecipe(recipe));
        }
    }
}
