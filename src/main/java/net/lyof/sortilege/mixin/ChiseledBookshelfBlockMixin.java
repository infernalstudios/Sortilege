package net.lyof.sortilege.mixin;

import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChiseledBookShelfBlock.class)
public class ChiseledBookshelfBlockMixin {
    @Inject(method = "removeBook", at = @At("HEAD"), cancellable = true)
    private static void authorRemoval(Level world, BlockPos pos, Player player, ChiseledBookShelfBlockEntity blockEntity,
                                      int slot, CallbackInfo ci) {
        ItemStack stack = blockEntity.getItem(slot);
        if (stack.is(ModItems.KNOWLEDGE_BOOK) && !KnowledgeBookItem.isAuthor(stack, player)) {
            player.displayClientMessage(Component.translatable("screen.sortilege.knowledge_book.invalid").withStyle(ChatFormatting.YELLOW), true);
            ci.cancel();
        }
    }

    @Inject(method = "addBook", at = @At("HEAD"), cancellable = true)
    private static void authorAddition(Level world, BlockPos pos, Player player, ChiseledBookShelfBlockEntity blockEntity,
                                       ItemStack stack, int slot, CallbackInfo ci) {
        if (stack.is(ModItems.KNOWLEDGE_BOOK) && !KnowledgeBookItem.isAuthor(stack, player)) {
            player.displayClientMessage(Component.translatable("screen.sortilege.knowledge_book.invalid").withStyle(ChatFormatting.YELLOW), true);
            ci.cancel();
        }
    }
}
