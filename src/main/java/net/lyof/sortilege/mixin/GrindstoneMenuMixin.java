package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.grinding.GrindingSlot;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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

    @ModifyReturnValue(method = "removeNonCursesFrom", at = @At("RETURN"))
    private ItemStack grindLearnable(ItemStack original) {
        original.remove(ModDataComponents.LEARNABLE);
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
        if (ModConfig.knowledgeEnabled.get()) {
            ItemStack[] stacks = new ItemStack[2];
            stacks[0] = this.repairSlots.getItem(0);
            stacks[1] = this.repairSlots.getItem(1);

            if (stacks[0].has(ModDataComponents.KNOWLEDGE) && stacks[1].has(ModDataComponents.KNOWLEDGE)) {
                ItemStack result = stacks[0].copy();
                EnchantKnowledge knowledge = result.get(ModDataComponents.KNOWLEDGE);
                for (Map.Entry<Holder<Enchantment>, Integer> entry : stacks[1].get(ModDataComponents.KNOWLEDGE).getEntries())
                    knowledge.learn(entry.getKey(), entry.getValue());
                result.set(ModDataComponents.KNOWLEDGE, knowledge);
                this.resultSlots.setItem(0, result);
                this.broadcastChanges();
                ci.cancel();
                return;
            }

            int i = -1;
            if (stacks[0].has(ModDataComponents.KNOWLEDGE)) i = 0;
            if (stacks[1].has(ModDataComponents.KNOWLEDGE)) i = 1;

            if (i != -1) {
                EnchantKnowledge knowledge = new EnchantKnowledge();
                knowledge.learn(stacks[i]);

                boolean flag = false;
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : stacks[1 - i].getEnchantments().entrySet()) {
                    if (knowledge.isLearnable(stacks[1 - i], entry.getKey(), entry.getIntValue())) {
                        knowledge.learn(entry.getKey(), entry.getIntValue());
                        flag = true;
                    }
                }

                if (flag) {
                    ItemStack result = stacks[i].copy();
                    result.set(ModDataComponents.KNOWLEDGE, knowledge);
                    this.resultSlots.setItem(0, result);
                    this.broadcastChanges();
                    ci.cancel();
                }
            }
        }
    }
}
