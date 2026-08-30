package net.lyof.sortilege.recipe.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.recipe.EmiCompat;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CauldronBrewingEmiRecipe extends BasicEmiRecipe {
    private final EmiIngredient input;
    private final EmiStack output;

    public CauldronBrewingEmiRecipe(RecipeHolder<CauldronBrewingRecipe> recipe) {
        super(EmiCompat.CAULDRON_CATEGORY, recipe.id(), 90, 18);
        this.input = EmiIngredient.of(recipe.value().input);
        this.output = EmiStack.of(PotionContents.createItemStack(Items.POTION, recipe.value().output));

        this.inputs.add(this.input);
        this.outputs.add(this.output);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(this.input, 18, 0);
        widgets.addSlot(EmiIngredient.of(BlockTags.CAMPFIRES), 0, 0);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 42, 1);
        widgets.addSlot(this.output, 72, 0).recipeContext(this);
    }
}
