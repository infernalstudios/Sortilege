package net.lyof.sortilege.mixin;

import net.lyof.sortilege.item.ModDataComponents;
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
        if (stack.has(ModDataComponents.KNOWLEDGE) && !stack.get(ModDataComponents.KNOWLEDGE).isAuthor(player)) {
            player.displayClientMessage(Component.translatable("screen.sortilege.knowledge_book.invalid").withStyle(ChatFormatting.YELLOW), true);
            ci.cancel();
        }
    }

    @Inject(method = "addBook", at = @At("HEAD"), cancellable = true)
    private static void authorAddition(Level world, BlockPos pos, Player player, ChiseledBookShelfBlockEntity blockEntity,
                                       ItemStack stack, int slot, CallbackInfo ci) {
        if (stack.has(ModDataComponents.KNOWLEDGE) && !stack.get(ModDataComponents.KNOWLEDGE).isAuthor(player)) {
            player.displayClientMessage(Component.translatable("screen.sortilege.knowledge_book.invalid").withStyle(ChatFormatting.YELLOW), true);
            ci.cancel();
        }
    }
}
