package net.lyof.sortilege.recipe.brewing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CauldronBrewingRecipe implements Recipe<SimpleContainer> {
    public final Ingredient input;
    public final Potion output;
    public final ResourceLocation id;

    public CauldronBrewingRecipe(Ingredient input, Potion output, ResourceLocation id) {
        this.input = input;
        this.output = output;
        this.id  = id;
    }

    @Override
    public boolean matches(SimpleContainer inventory, Level world) {
        return this.input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer inventory, RegistryAccess registryManager) {
        return this.getResultItem(registryManager);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), this.output);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_BREWING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CAULDRON_BREWING;
    }

    public static class Serializer implements RecipeSerializer<CauldronBrewingRecipe> {
        public CauldronBrewingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new CauldronBrewingRecipe(Ingredient.fromJson(json.get("input")),
                    BuiltInRegistries.POTION.get(new ResourceLocation(GsonHelper.getAsString(json, "output"))),
                    id);
        }

        @Override
        public CauldronBrewingRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            Ingredient input = Ingredient.fromNetwork(packetByteBuf);
            Potion output = BuiltInRegistries.POTION.get(packetByteBuf.readResourceLocation());
            return new CauldronBrewingRecipe(input, output, identifier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetByteBuf, CauldronBrewingRecipe recipe) {
            recipe.input.toNetwork(packetByteBuf);
            packetByteBuf.writeResourceLocation(BuiltInRegistries.POTION.getKey(recipe.output));
        }
    }
}
