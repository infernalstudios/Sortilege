package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(value = PlayerInventory.class, priority = 1001)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @Shadow public abstract ItemStack getMainHandStack();

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int skipEquipped(List<ItemStack> list, Operation<Integer> original) {
        if ((list != this.main && ConfigEntries.keepEquipped)) {
            return 0;
        }
        return original.call(list);
    }

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.isEmpty()Z"))
    public boolean skipHotbar(ItemStack stack, Operation<Boolean> original, @Local(index = 3) int i) {
        if (i < PlayerInventory.getHotbarSize() && ConfigEntries.keepEquipped)
            return true;

        if (ItemHelper.hasEnchant(ModEnchants.SOULBOUND, stack)) {
            if (ConfigEntries.consumeSoulbound && ModEnchants.SOULBOUND != null) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                enchants.remove(ModEnchants.SOULBOUND);
            }
            return true;
        }
        if (stack.isIn(ModTags.Items.KEEP_ON_DEATH)) return true;

        return original.call(stack);
    }

    @WrapMethod(method = "dropSelectedItem")
    private ItemStack preventStorytoldDrop(boolean entireStack, Operation<ItemStack> original) {
        if (ItemHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, this.getMainHandStack()))
            return ItemStack.EMPTY;
        return original.call(entireStack);
    }
}
