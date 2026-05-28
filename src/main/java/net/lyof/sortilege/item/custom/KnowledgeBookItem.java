package net.lyof.sortilege.item.custom;

import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.factory.KnowledgeBookScreenFactory;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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
}
