package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.factory.KnowledgeBookScreenFactory;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBookItem extends Item {
    private static ItemStack knowledgeCacher = null;
    private static EnchantKnowledge knowledge = null;
    private static List<String> authors = null;

    public static void fillItemGroup(FabricItemGroupEntries entries, Item previous) {
        if (!ModConfig.knowledgeEnabled.get()) return;

        EnchantKnowledge knowledge = new EnchantKnowledge();
        for (Enchantment enchant : BuiltInRegistries.ENCHANTMENT)
            knowledge.learn(enchant, enchant.getMaxLevel());

        ItemStack full = ModItems.KNOWLEDGE_BOOK.getDefaultInstance();
        setKnowledge(full, knowledge);
        entries.addAfter(previous, full);
        entries.addAfter(previous, ModItems.KNOWLEDGE_BOOK);
    }

    public KnowledgeBookItem(Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!isAuthor(stack, user)) {
            user.displayClientMessage(Component.translatable("item.sortilege.knowledge_book.invalid").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.success(stack);
        }
        if (!world.isClientSide())
            user.openMenu(new KnowledgeBookScreenFactory(stack));
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag context) {
        super.appendHoverText(stack, level, list, context);

        if (Screen.hasShiftDown()) {
            list.add(Component.translatable("item.sortilege.knowledge_book.desc0").withStyle(ChatFormatting.YELLOW));
            list.add(Component.translatable("item.sortilege.knowledge_book.desc1").withStyle(ChatFormatting.YELLOW));
        } else
            list.add(EnchantHelper.getShiftTooltip());

        if (!getAuthors(stack).isEmpty())
            list.add(Component.translatable("book.byAuthor", String.join(", ", getAuthors(stack))).withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable("item.sortilege.knowledge_book.completion", getKnowledge(stack).getEntries().size(),
                EnchantHelper.getEnchantCount()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        super.onCraftedBy(stack, world, player);
        setAuthors(stack, List.of(player.getScoreboardName()));
    }

    private static void buildCache(ItemStack self) {
        knowledgeCacher = self;

        knowledge = EnchantKnowledge.read(self.getOrCreateTag());
        authors = new ArrayList<>();
        if (!self.getOrCreateTag().contains(EnchantKnowledge.AUTHORS_KEY, Tag.TAG_LIST))
            return;
        for (Tag elt : self.getTag().getList(EnchantKnowledge.AUTHORS_KEY, Tag.TAG_STRING))
            authors.add(elt.getAsString());
    }

    public static EnchantKnowledge getKnowledge(ItemStack self) {
        if (self == knowledgeCacher) return knowledge;
        buildCache(self);

        return knowledge;
    }

    public static void setKnowledge(ItemStack self, EnchantKnowledge knowledge) {
        if (self == knowledgeCacher) knowledgeCacher = null;

        self.getOrCreateTag().put(EnchantKnowledge.KNOWLEDGE_KEY, knowledge.write(new CompoundTag()));
    }

    public static List<String> getAuthors(ItemStack self) {
        if (self == knowledgeCacher) return authors;
        buildCache(self);

        return authors;
    }

    public static void setAuthors(ItemStack self, List<String> authors) {
        if (self == knowledgeCacher) knowledgeCacher = null;

        ListTag list = new ListTag();
        for (String author : authors) list.add(StringTag.valueOf(author));
        self.getOrCreateTag().put(EnchantKnowledge.AUTHORS_KEY, list);
    }

    public static boolean isAuthor(ItemStack self, Player player) {
        return player.isCreative() || getAuthors(self).contains(player.getScoreboardName()) || getAuthors(self).isEmpty();
    }
}
