package net.lyof.sortilege.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.Holder;
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
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        if ((!ModConfig.allowInventoryEnchanting.get() && !player.isCreative()) || clickType == ClickAction.PRIMARY) return;
        if (!(stack.getItem() instanceof EnchantedBookItem)) return;

        ItemEnchantments enchants = stack.getEnchantments();
        ItemStack other = slot.getItem();
        boolean used = false;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.entrySet()) {
            if (entry.getKey().value().canEnchant(other)
                    && EnchantmentHelper.isEnchantmentCompatible(other.getEnchantments().keySet(), entry.getKey())) {
                other.enchant(entry.getKey(), entry.getIntValue());
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
