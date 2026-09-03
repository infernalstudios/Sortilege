package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @WrapOperation(method = "setEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T> T setEnchantments(ItemStack instance, DataComponentType<T> component, T value, Operation<T> original) {
        int limit = EnchantHelper.getTotalEnchantSlots(instance);
        if (!instance.is(Items.ENCHANTED_BOOK) && limit >= 0) {
            ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            boolean curse;
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : ((ItemEnchantments) value).entrySet()) {
                curse = ModConfig.cursesAddSlots.get() && entry.getKey().is(EnchantmentTags.CURSE);
                if (limit > 0 || curse)
                    enchants.set(entry.getKey(), entry.getIntValue());
                if (!curse) limit--;
            }

            value = (T) enchants.toImmutable();
        }

        return original.call(instance, component, value);
    }

    @Inject(method = "getEnchantmentCost", at = @At("HEAD"), cancellable = true)
    private static void betterEnchantingCosts(RandomSource random, int slotIndex, int bookshelfCount, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.doIncreasedEnchantCosts.get() && ModConfig.increasedEnchantNeeds.get().size() == 3) {
            int bonus = (int) ((slotIndex * 0.5) * bookshelfCount);
            cir.setReturnValue(ModConfig.increasedEnchantNeeds.get().get(slotIndex) + bonus);
        }
    }
}
