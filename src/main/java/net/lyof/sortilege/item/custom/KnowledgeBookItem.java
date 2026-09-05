package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.List;

public class KnowledgeBookItem extends Item {
    public static void fillItemGroup(FabricItemGroupEntries entries, Item previous) {
        if (!ModConfig.knowledgeEnabled.get()) return;

        EnchantKnowledge knowledge = new EnchantKnowledge();
        EnchantHelper.iterateRegistry(enchant -> knowledge.learn(enchant, enchant.value().getMaxLevel()));
        ItemStack full = ModItems.KNOWLEDGE_BOOK.getDefaultInstance();
        full.set(ModDataComponents.KNOWLEDGE, knowledge);
        entries.addAfter(previous, full);
        entries.addAfter(previous, ModItems.KNOWLEDGE_BOOK);
    }

    public KnowledgeBookItem(Properties settings) {
        super(settings.component(ModDataComponents.KNOWLEDGE, new EnchantKnowledge()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!stack.get(ModDataComponents.KNOWLEDGE).isAuthor(user)) {
            user.displayClientMessage(Component.translatable("screen.sortilege.knowledge_book.invalid").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.success(stack);
        }
        if (!world.isClientSide())
            user.openMenu(KnowledgeBookScreenHandler.getFactory(stack));
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, context, list, flag);

        if (Screen.hasShiftDown()) {
            for (String s : Component.translatable("item.sortilege.knowledge_book.desc").getString().split("\n"))
                list.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
        } else
            list.add(EnchantHelper.getShiftTooltip());

        if (!stack.get(ModDataComponents.KNOWLEDGE).getAuthors().isEmpty())
            list.add(Component.translatable("book.byAuthor", String.join(", ", stack.get(ModDataComponents.KNOWLEDGE).getAuthors())).withStyle(ChatFormatting.GRAY));

        float completion = 100f * stack.get(ModDataComponents.KNOWLEDGE).getCompletion() / EnchantHelper.getEnchantCount();
        String c = completion == (int) completion ? (int) completion + "%" : String.format("%.1f", completion) + "%";
        list.add(Component.translatable("tooltip.sortilege.knowledge_book.completion", c).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        super.onCraftedBy(stack, world, player);
        stack.get(ModDataComponents.KNOWLEDGE).setAuthors(List.of(player.getScoreboardName()));
    }
}
