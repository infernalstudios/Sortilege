package net.lyof.sortilege.recipe.brewing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class CauldronBrewingRecipe implements Recipe<SimpleInventory> {
    public final Ingredient input;
    public final Potion output;
    public final Identifier id;

    public CauldronBrewingRecipe(Ingredient input, Potion output, Identifier id) {
        this.input = input;
        this.output = output;
        this.id  = id;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return this.input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return this.getOutput(registryManager);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return PotionUtil.setPotion(Items.POTION.getDefaultStack(), this.output);
    }

    @Override
    public Identifier getId() {
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
        public CauldronBrewingRecipe read(Identifier id, JsonObject json) {
            return new CauldronBrewingRecipe(Ingredient.fromJson(json.get("input")),
                    Registries.POTION.get(new Identifier(JsonHelper.getString(json, "output"))),
                    id);
        }

        public CauldronBrewingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Ingredient input = Ingredient.fromPacket(packetByteBuf);
            Potion output = Registries.POTION.get(packetByteBuf.readIdentifier());
            return new CauldronBrewingRecipe(input, output, identifier);
        }

        public void write(PacketByteBuf packetByteBuf, CauldronBrewingRecipe recipe) {
            recipe.input.write(packetByteBuf);
            packetByteBuf.writeIdentifier(Registries.POTION.getId(recipe.output));
        }
    }
}
