package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int skipEquipped(List list, Operation<Integer> original) {
        if ((list != this.main && ConfigEntries.keepEquipped)) {
            return 0;
        }
        return original.call(list);
    }

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    public Object skipHotbar(List<ItemStack> list, int i, Operation<Object> original) {
        if (i < PlayerInventory.getHotbarSize() && ConfigEntries.keepEquipped)
            return ItemStack.EMPTY;

        ItemStack stack = (ItemStack) original.call(list, i);
        if (ItemHelper.hasEnchant(ModEnchants.SOULBOUND, stack)) {
            if (ConfigEntries.consumeSoulbound && ModEnchants.SOULBOUND != null) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                enchants.remove(ModEnchants.SOULBOUND);
            }
            return ItemStack.EMPTY;
        }
        if (stack.isIn(ModTags.Items.KEEP_ON_DEATH)) return ItemStack.EMPTY;

        return stack;
    }
}
