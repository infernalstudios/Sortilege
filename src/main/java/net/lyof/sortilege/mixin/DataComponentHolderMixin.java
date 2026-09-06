package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.enchant.BuiltInEnchantsItem;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentHolder.class)
public interface DataComponentHolderMixin {
    @WrapMethod(method = "get")
    private <T> T builtinEnchants(DataComponentType<? extends T> component, Operation<T> original) {
        if (
            component == DataComponents.ENCHANTMENTS
            && (DataComponentHolder) (Object) this instanceof ItemStack stack
            && stack.getItem() instanceof BuiltInEnchantsItem builtin
            && !stack.has(ModDataComponents.BUILT_IN_ENCHANTS)
        ) {
            ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable((ItemEnchantments) original.call(component));

            if (!EnchantHelper.iterateRegistry(enchant -> {
                ResourceLocation id = enchant.unwrapKey().map(ResourceKey::location).orElse(null);
                if (id == null) return;

                int level = builtin.getBuiltinEnchantments().getOrDefault(id, -1);
                if (level == -1) return;

                enchants.upgrade(enchant, level);
            })) return original.call(component);

            stack.set(DataComponents.ENCHANTMENTS, enchants.toImmutable());
            stack.set(ModDataComponents.BUILT_IN_ENCHANTS, Unit.INSTANCE);
        }
        return original.call(component);
    }
}
