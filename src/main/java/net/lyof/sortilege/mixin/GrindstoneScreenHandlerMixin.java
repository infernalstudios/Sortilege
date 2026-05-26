package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.grinding.GrindingSlot;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    protected GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow @Final Inventory input;
    @Shadow @Final private Inventory result;

    @ModifyReturnValue(method = "grind", at = @At("RETURN"))
    private ItemStack grindLearnable(ItemStack original) {
        original.removeSubNbt(EnchantKnowledge.LEARNABLE_KEY);
        return original;
    }

    @WrapOperation(
            method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;")
    )
    private Slot initSlot(GrindstoneScreenHandler instance, Slot slot, Operation<Slot> original) {
        return slot.inventory == this.input ? original.call(instance, new GrindingSlot(slot)) : original.call(instance, slot);
    }

    @WrapMethod(method = "updateResult")
    private void updateLearningResult(Operation<Void> original) {
        if (ConfigEntries.knowledgeEnabled) {
            ItemStack[] stacks = new ItemStack[2];
            stacks[0] = this.input.getStack(0);
            stacks[1] = this.input.getStack(1);

            if (stacks[0].isOf(ModItems.KNOWLEDGE_BOOK) && stacks[1].isOf(ModItems.KNOWLEDGE_BOOK)) {
                ItemStack result = stacks[0].copy();
                KnowledgeBookItem.learn(result, stacks[1]);
                this.result.setStack(0, result);
                this.sendContentUpdates();
                return;
            }

            int i = -1;
            if (stacks[0].isOf(ModItems.KNOWLEDGE_BOOK)) i = 0;
            if (stacks[1].isOf(ModItems.KNOWLEDGE_BOOK)) i = 1;

            if (i != -1) {
                EnchantKnowledge knowledge = new EnchantKnowledge();
                knowledge.learn(stacks[i]);

                boolean flag = false;
                for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(stacks[1 - i]).entrySet()) {
                    if (knowledge.isLearnable(stacks[1 - i], entry.getKey(), entry.getValue())) {
                        knowledge.learn(entry.getKey(), entry.getValue());
                        flag = true;
                    }
                }

                if (flag) {
                    ItemStack result = stacks[i].copy();
                    KnowledgeBookItem.setKnowledge(result, knowledge);
                    this.result.setStack(0, result);
                    this.sendContentUpdates();
                    return;
                }
            }
        }

        original.call();
    }
}
