package net.lyof.sortilege.recipe.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpecialSmithingEmiRecipe extends BasicEmiRecipe {
    protected final int uniq;
    protected List<Item> bases;
    protected EmiIngredient additions;
    protected SmithingRecipe recipe;

    public SpecialSmithingEmiRecipe(SmithingRecipe recipe, EmiIngredient additions) {
        super(VanillaEmiRecipeCategories.SMITHING, recipe.getId(), 112, 18);
        this.uniq = MathHelper.rnd.nextInt();
        this.bases = new ArrayList<>();
        this.additions = additions;
        this.recipe = recipe;
        this.generateInputs();
    }

    protected void generateInputs() {
        for (Item item : Registries.ITEM) {
            if (this.recipe.testBase(item.getDefaultStack())) this.bases.add(item);
        }
        this.inputs.addAll(this.bases.stream().map(EmiStack::of).toList());
        this.inputs.add(this.additions);
        this.inputs.add(EmiStack.of(Items.LAPIS_LAZULI));

        this.outputs.addAll(this.bases.stream().map(EmiStack::of).toList());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiStack.of(Items.LAPIS_LAZULI), 0, 0);
        widgets.addGeneratedSlot(random -> this.getStack(random).get(0), this.uniq, 18, 0)
                .appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(this.bases.stream().map(EmiStack::of).toList()));
        widgets.addSlot(this.additions, 36, 0);

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);

        widgets.addGeneratedSlot(random -> this.getStack(random).get(1), this.uniq, 94, 0).recipeContext(this);
    }

    private List<EmiStack> getStack(Random random) {
        int i = random.nextInt(this.bases.size());
        EmiStack input = this.inputs.get(i).getEmiStacks().get(0);

        return List.of(input, EmiStack.of(this.recipe.craft(new SimpleInventory(
                Items.LAPIS_LAZULI.getDefaultStack(), input.getItemStack(), this.additions.getEmiStacks().get(0).getItemStack()),
                MinecraftClient.getInstance().world.getRegistryManager())));
    }
}
