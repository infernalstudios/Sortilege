package net.lyof.sortilege.recipe.crafting;

import net.lyof.sortilege.item.custom.staff.BotaniaManaStaffItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.common.crafting.recipe.NoOpRecipeSerializer;
import vazkii.botania.common.crafting.recipe.RecipeUtils;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class BotaniaStaffLensRecipe extends CustomRecipe {
    public static final NoOpRecipeSerializer<BotaniaStaffLensRecipe> SERIALIZER = new NoOpRecipeSerializer<>(BotaniaStaffLensRecipe::new);

    public BotaniaStaffLensRecipe(ResourceLocation id) {
        super(id, CraftingBookCategory.EQUIPMENT);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        int foundLens = 0;
        int foundStaff = 0;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof BotaniaManaStaffItem) {
                    foundStaff++;
                } else {
                    if (!ManaBlasterItem.isValidLens(stack))
                        return false;

                    foundLens++;
                }
            }
        }

        return foundLens <= 1 && foundStaff == 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
        ItemStack lens = ItemStack.EMPTY;
        ItemStack staff = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof BotaniaManaStaffItem)
                    staff = stack;
                else if (stack.getItem() instanceof BasicLensItem)
                    lens = stack.copyWithCount(1);
            }
        }

        if (!staff.isEmpty()) {
            BotaniaManaStaffItem item = ((BotaniaManaStaffItem) staff.getItem());
            if (item.getLens(staff).isEmpty() && lens.isEmpty())
                return ItemStack.EMPTY;

            ItemStack result = staff.copy();
            item.setLens(result, lens);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return RecipeUtils.getRemainingItemsSub(inv, (s) -> {
            if (s.getItem() instanceof BotaniaManaStaffItem staff) {
                ItemStack stack = staff.getLens(s);
                stack.setCount(1);
                return stack;
            } else {
                return null;
            }
        });
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}