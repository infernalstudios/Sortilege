package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    public void preventUselessEnchants(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        int a = EnchantHelper.getUsedEnchantSlots(stack);
        int m = EnchantHelper.getTotalEnchantSlots(stack);

        if (m >= 0 && a >= m) cir.setReturnValue(false);
    }

    @Inject(method = "overrideStackedOnOther", at = @At("TAIL"), cancellable = true)
    public void inventoryEnchant(ItemStack stack, Slot slot, ClickAction clickType, Player player, CallbackInfoReturnable<Boolean> cir) {
        if ((!ConfigEntries.allowInventoryEnchanting && !player.isCreative()) || clickType == ClickAction.PRIMARY) return;
        if (!(stack.getItem() instanceof EnchantedBookItem)) return;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        ItemStack other = slot.getItem();
        boolean used = false;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getKey().canEnchant(other)
                    && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(other).keySet(), entry.getKey())) {
                other.enchant(entry.getKey(), entry.getValue());
                used = true;
            }
        }

        if (used) {
            if (player.level().isClientSide()) {
                for (int i = 0; i < 20; i++) {
                    float sin = (float) Math.sin(i * Math.PI / 10);
                    float cos = (float) Math.cos(i * Math.PI / 10);

                    player.level().addParticle(ParticleTypes.END_ROD,
                            player.getX() + sin, player.getEyeY() - 0.5, player.getZ() + cos,
                            0, 0, 0);
                }
            }

            player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);

            stack.shrink(1);
            cir.setReturnValue(true);
        }
    }
}
