package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.grinding.GrindingSlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu {
    @Shadow @Final Container repairSlots;
    @Shadow @Final private Container resultSlots;

    protected GrindstoneMenuMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @ModifyReturnValue(method = "removeNonCurses", at = @At("RETURN"))
    private ItemStack grindLearnable(ItemStack original) {
        original.removeTagKey(EnchantKnowledge.LEARNABLE_KEY);
        return original;
    }

    @WrapOperation(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/GrindstoneMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;")
    )
    private Slot initSlot(GrindstoneMenu instance, Slot slot, Operation<Slot> original) {
        return slot.container == this.repairSlots ? original.call(instance, new GrindingSlot(slot)) : original.call(instance, slot);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void updateLearningResult(CallbackInfo ci) {
        if (ConfigEntries.knowledgeEnabled) {
            ItemStack[] stacks = new ItemStack[2];
            stacks[0] = this.repairSlots.getItem(0);
            stacks[1] = this.repairSlots.getItem(1);

            if (stacks[0].is(ModItems.KNOWLEDGE_BOOK) && stacks[1].is(ModItems.KNOWLEDGE_BOOK)) {
                ItemStack result = stacks[0].copy();
                EnchantKnowledge knowledge = KnowledgeBookItem.getKnowledge(result);
                for (Map.Entry<Enchantment, Integer> entry : KnowledgeBookItem.getKnowledge(stacks[1]).getEntries())
                    knowledge.learn(entry.getKey(), entry.getValue());
                KnowledgeBookItem.setKnowledge(result, knowledge);
                this.resultSlots.setItem(0, result);
                this.broadcastChanges();
                ci.cancel();
                return;
            }

            int i = -1;
            if (stacks[0].is(ModItems.KNOWLEDGE_BOOK)) i = 0;
            if (stacks[1].is(ModItems.KNOWLEDGE_BOOK)) i = 1;

            if (i != -1) {
                EnchantKnowledge knowledge = new EnchantKnowledge();
                knowledge.learn(stacks[i]);

                boolean flag = false;
                for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stacks[1 - i]).entrySet()) {
                    if (knowledge.isLearnable(stacks[1 - i], entry.getKey(), entry.getValue())) {
                        knowledge.learn(entry.getKey(), entry.getValue());
                        flag = true;
                    }
                }

                if (flag) {
                    ItemStack result = stacks[i].copy();
                    KnowledgeBookItem.setKnowledge(result, knowledge);
                    this.resultSlots.setItem(0, result);
                    this.broadcastChanges();
                    ci.cancel();
                }
            }
        }
    }
}
