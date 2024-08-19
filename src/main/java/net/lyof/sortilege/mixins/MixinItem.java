package net.lyof.sortilege.mixins;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(Item.class)
public class MixinItem {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void showEnchantLimit(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag, CallbackInfo ci) {
        int a = ItemHelper.getEnchantValue(itemstack);
        int m = ItemHelper.getMaxEnchantValue(itemstack);

        if ((a > 0 || ItemHelper.getExtraEnchants(itemstack) > 0 || ConfigEntries.alwaysShowEnchantLimit) &&
                m > 0 && itemstack.getEnchantmentValue() > 0 && !itemstack.is(Items.ENCHANTED_BOOK))

            list.add(Component.literal(a + "/" + m).append(" ")
                    .append(Component.translatable("sortilege.enchantments"))
                    .withStyle(a >= m ? ChatFormatting.RED : ChatFormatting.WHITE));
    }

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    public void preventUselessEnchants(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        int a = ItemHelper.getEnchantValue(stack);
        int m = ItemHelper.getMaxEnchantValue(stack);

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
            if (player.getLevel().isClientSide()) {
                for (int i = 0; i < 20; i++) {
                    float sin = (float) Math.sin(i * Math.PI / 10);
                    float cos = (float) Math.cos(i * Math.PI / 10);

                    player.getLevel().addParticle(ParticleTypes.END_ROD,
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
