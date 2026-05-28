package net.lyof.sortilege.mixin;

import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChiseledBookshelfBlock.class)
public class ChiseledBookshelfBlockMixin {
    @Inject(method = "tryRemoveBook", at = @At("HEAD"), cancellable = true)
    private static void authorRemoval(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity,
                                      int slot, CallbackInfo ci) {
        ItemStack stack = blockEntity.getStack(slot);
        if (stack.isOf(ModItems.KNOWLEDGE_BOOK) && !KnowledgeBookItem.isAuthor(stack, player)) {
            player.sendMessage(Text.translatable("item.sortilege.knowledge_book.invalid").formatted(Formatting.YELLOW), true);
            ci.cancel();
        }
    }

    @Inject(method = "tryAddBook", at = @At("HEAD"), cancellable = true)
    private static void authorAddition(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity,
                                       ItemStack stack, int slot, CallbackInfo ci) {
        if (stack.isOf(ModItems.KNOWLEDGE_BOOK) && !KnowledgeBookItem.isAuthor(stack, player)) {
            player.sendMessage(Text.translatable("item.sortilege.knowledge_book.invalid").formatted(Formatting.YELLOW), true);
            ci.cancel();
        }
    }
}
