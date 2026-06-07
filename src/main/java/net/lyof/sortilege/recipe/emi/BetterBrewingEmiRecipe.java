package net.lyof.sortilege.recipe.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Random;

public class BetterBrewingEmiRecipe extends BasicEmiRecipe {
    protected final BrewingRecipe recipe;
    protected final int uniq;
    private static final ResourceLocation BACKGROUND = ResourceLocation.tryBuild("minecraft", "textures/gui/container/brewing_stand.png");
    private static final EmiStack BLAZE_POWDER = EmiStack.of(Items.BLAZE_POWDER);

    public BetterBrewingEmiRecipe(BrewingRecipe recipe) {
        super(VanillaEmiRecipeCategories.BREWING, recipe.getId(), 120, 61);
        this.recipe = recipe;
        this.uniq = MathHelper.rnd.nextInt();
        this.inputs.add(EmiStack.of(recipe.getIngredient()));
        this.inputs.add(EmiStack.of(recipe.getInput()));
        this.outputs.add(EmiStack.of(recipe.getOutput()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 103, 61, 16, 14);
        widgets.addAnimatedTexture(BACKGROUND, 81, 2, 9, 28, 176, 0, 20000, false, false, false)
                .tooltip((mx, my) -> List.of(ClientTooltipComponent.create(Component.translatable("emi.cooking.time", 20).getVisualOrderText())));
        widgets.addAnimatedTexture(BACKGROUND, 47, 0, 12, 29, 185, 0, 700, false, true, false);
        widgets.addTexture(BACKGROUND, 44, 30, 18, 4, 176, 29);
        widgets.addSlot(BLAZE_POWDER, 0, 2).drawBack(false);
        widgets.addGeneratedSlot(random -> getStack(random).get(0), this.uniq, 39, 36).drawBack(false);
        widgets.addSlot(EmiStack.of(this.recipe.getIngredient()), 62, 2).drawBack(false);
        widgets.addGeneratedSlot(random -> getStack(random).get(1), this.uniq, 85, 36).drawBack(false).recipeContext(this);
    }

    private List<EmiStack> getStack(Random random) {
        EmiStack input = EmiStack.of(this.recipe.getInput(random));
        return List.of(input, EmiStack.of(this.recipe.craft(input.getItemStack(), this.recipe.getIngredient())));
    }
}
