package net.lyof.sortilege.recipe.enchanting.catalyst;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.*;

public record CatalystRecipe(Item item, List<Holder<Enchantment>> enchants) implements Recipe<SingleRecipeInput> {
    public static boolean isDisabled(Level world) {
        return !ModConfig.catalystBooks.get() && world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CATALYST).isEmpty();
    }

    public static ItemEnchantments getEnchantments(ItemStack catalyst, Level world) {
        if (catalyst.is(Items.ENCHANTED_BOOK) && ModConfig.catalystBooks.get())
            return catalyst.getEnchantments();

        ItemEnchantments.Mutable result = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (RecipeHolder<CatalystRecipe> recipe : world.getRecipeManager().getRecipesFor(ModRecipeTypes.CATALYST, new SingleRecipeInput(catalyst), world))
            for (Holder<Enchantment> enchant : recipe.value().enchants())
                result.set(enchant, 1);
        return result.toImmutable();
    }

    public static boolean isCatalyst(ItemStack item, Level world) {
        return world.getRecipeManager().getRecipeFor(ModRecipeTypes.CATALYST, new SingleRecipeInput(item), world).isPresent()
                || (item.is(Items.ENCHANTED_BOOK) && ModConfig.catalystBooks.get());
    }


    @Override
    public String toString() {
        return "CatalystRecipe{" +
                "item=" + item +
                ", enchants=" + enchants.size() +
                '}';
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return input.getItem(0).is(this.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.item().getDefaultInstance();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CATALYST_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CATALYST;
    }


    public static class Serializer implements RecipeSerializer<CatalystRecipe> {
        private static final MapCodec<CatalystRecipe> codec =
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("input").forGetter(CatalystRecipe::item),
                                        Enchantment.CODEC.listOf().fieldOf("enchantments").forGetter(CatalystRecipe::enchants))
                                .apply(instance, CatalystRecipe::new)
                );
        private static final StreamCodec<RegistryFriendlyByteBuf, CatalystRecipe> streamCodec =
                StreamCodec.of(CatalystRecipe.Serializer::toNetwork, CatalystRecipe.Serializer::fromNetwork);


        @Override
        public MapCodec<CatalystRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CatalystRecipe> streamCodec() {
            return streamCodec;
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, CatalystRecipe recipe) {
            buf.writeUtf(BuiltInRegistries.ITEM.getKey(recipe.item()).toString());
            buf.writeInt(recipe.enchants().size());
            recipe.enchants().forEach(enchant -> Enchantment.STREAM_CODEC.encode(buf, enchant));
        }

        public static CatalystRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Item item = BuiltInRegistries.ITEM.get(Identifier.of(buf.readUtf()));
            int size = buf.readInt();
            List<Holder<Enchantment>> enchants = new ArrayList<>(size);
            for (int i = 0; i < size; i++)
                enchants.add(Enchantment.STREAM_CODEC.decode(buf));
            return new CatalystRecipe(item, enchants);
        }
    }
}
