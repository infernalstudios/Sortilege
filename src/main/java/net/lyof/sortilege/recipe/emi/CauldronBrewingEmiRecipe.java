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
import net.minecraft.world.item.alchemy.PotionUtils;

public class CauldronBrewingEmiRecipe extends BasicEmiRecipe {
    private final EmiIngredient input;
    private final EmiStack output;

    public CauldronBrewingEmiRecipe(CauldronBrewingRecipe recipe) {
        super(EmiCompat.CAULDRON_CATEGORY, recipe.getId(), 90, 18);
        this.input = EmiIngredient.of(recipe.input);
        this.output = EmiStack.of(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), recipe.output));

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
