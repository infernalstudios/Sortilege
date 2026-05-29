package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItemGroups;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.factory.KnowledgeBookScreenFactory;
import net.lyof.sortilege.util.ItemHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBookItem extends Item {
    private static ItemStack knowledgeCacher = null;
    private static EnchantKnowledge knowledge = null;
    private static List<String> authors = null;

    public static void fillItemGroup(FabricItemGroupEntries entries, Item previous) {
        if (!ConfigEntries.knowledgeEnabled) return;

        EnchantKnowledge knowledge = new EnchantKnowledge();
        for (Enchantment enchant : Registries.ENCHANTMENT)
            knowledge.learn(enchant, enchant.getMaxLevel());

        ItemStack full = ModItems.KNOWLEDGE_BOOK.getDefaultStack();
        setKnowledge(full, knowledge);
        entries.addAfter(previous, full);
        entries.addAfter(previous, ModItems.KNOWLEDGE_BOOK);
    }

    public KnowledgeBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!isAuthor(stack, user)/* && !FabricLoader.getInstance().isDevelopmentEnvironment()*/) {
            user.sendMessage(Text.translatable("item.sortilege.knowledge_book.invalid").formatted(Formatting.YELLOW), true);
            return TypedActionResult.success(stack);
        }
        if (!world.isClient())
            user.openHandledScreen(new KnowledgeBookScreenFactory(stack));
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext context) {
        super.appendTooltip(stack, level, list, context);

        if (Screen.hasShiftDown()) {
            list.add(Text.translatable("item.sortilege.knowledge_book.desc0").formatted(Formatting.YELLOW));
            list.add(Text.translatable("item.sortilege.knowledge_book.desc1").formatted(Formatting.YELLOW));
        } else
            list.add(ItemHelper.getShiftTooltip());

        if (!getAuthors(stack).isEmpty())
            list.add(Text.translatable("book.byAuthor", getAuthors(stack).get(0)).formatted(Formatting.GRAY));
        list.add(Text.translatable("item.sortilege.knowledge_book.completion", getKnowledge(stack).getEntries().size(),
                ItemHelper.getEnchantCount()).formatted(Formatting.GRAY));
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        super.onCraft(stack, world, player);
        setAuthors(stack, List.of(player.getEntityName()));
    }

    private static void buildCache(ItemStack self) {
        knowledgeCacher = self;

        knowledge = EnchantKnowledge.read(self.getOrCreateNbt());
        authors = new ArrayList<>();
        if (!self.getOrCreateNbt().contains(EnchantKnowledge.AUTHORS_KEY, NbtElement.LIST_TYPE))
            return;
        for (NbtElement elt : self.getNbt().getList(EnchantKnowledge.AUTHORS_KEY, NbtElement.STRING_TYPE))
            authors.add(elt.asString());
    }

    public static EnchantKnowledge getKnowledge(ItemStack self) {
        if (self == knowledgeCacher) return knowledge;
        buildCache(self);

        return knowledge;
    }

    public static void setKnowledge(ItemStack self, EnchantKnowledge knowledge) {
        if (self == knowledgeCacher) knowledgeCacher = null;

        self.getOrCreateNbt().put(EnchantKnowledge.KNOWLEDGE_KEY, knowledge.write(new NbtCompound()));
    }

    public static List<String> getAuthors(ItemStack self) {
        if (self == knowledgeCacher) return authors;
        buildCache(self);

        return authors;
    }

    public static void setAuthors(ItemStack self, List<String> authors) {
        if (self == knowledgeCacher) knowledgeCacher = null;

        NbtList list = new NbtList();
        for (String author : authors) list.add(NbtString.of(author));
        self.getOrCreateNbt().put(EnchantKnowledge.AUTHORS_KEY, list);
    }

    public static boolean isAuthor(ItemStack self, PlayerEntity player) {
        return player.isCreative() || getAuthors(self).contains(player.getEntityName()) || getAuthors(self).isEmpty();
    }
}
