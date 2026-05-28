package net.lyof.sortilege.item.custom;

import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.factory.KnowledgeBookScreenFactory;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class KnowledgeBookItem extends Item {
    private static ItemStack knowledgeCacher = null;
    private static EnchantKnowledge knowledge = null;

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
        if (!world.isClient() && !getKnowledge(stack).getEntries().isEmpty())
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

    public static void learn(ItemStack self, ItemStack stack) {
        EnchantKnowledge knowledge = getKnowledge(self);

        for (Map.Entry<Enchantment, Integer> entry : stack.getItem() instanceof KnowledgeBookItem
                ? getKnowledge(stack).getEntries() : EnchantmentHelper.get(stack).entrySet())
            knowledge.learn(entry.getKey(), entry.getValue());

        setKnowledge(self, knowledge);
    }

    public static EnchantKnowledge getKnowledge(ItemStack self) {
        if (self == knowledgeCacher) return knowledge;

        knowledge = EnchantKnowledge.read(self.getOrCreateNbt());
        knowledgeCacher = self;
        return knowledge;
    }

    public static void setKnowledge(ItemStack self, EnchantKnowledge knowledge) {
        self.getOrCreateNbt().put(EnchantKnowledge.KNOWLEDGE_KEY, knowledge.write(new NbtCompound()));
    }
}
