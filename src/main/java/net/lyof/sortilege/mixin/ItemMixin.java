package net.lyof.sortilege.mixin;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void showEnchantLimit(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        int a = ItemHelper.getEnchantValue(stack);
        int m = ItemHelper.getMaxEnchantValue(stack);

        if ((a > 0 || ItemHelper.getExtraEnchants(stack) > 0 || ConfigEntries.alwaysShowEnchantLimit) &&
                m > 0 && stack.getItem().getEnchantability() > 0 && !stack.isOf(Items.ENCHANTED_BOOK))

            tooltip.add(Text.literal(a + "/" + m).append(" ")
                    .append(Text.translatable("sortilege.enchantments"))
                    .formatted(a >= m ? Formatting.RED : Formatting.WHITE));
    }

    @Inject(method = "onStackClicked", at = @At("TAIL"), cancellable = true)
    public void inventoryEnchant(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if ((!ConfigEntries.allowInventoryEnchanting && !player.isCreative()) || clickType == ClickType.LEFT) return;
        if (!(stack.getItem() instanceof EnchantedBookItem)) return;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        ItemStack other = slot.getStack();
        boolean used = false;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getKey().isAcceptableItem(other)
                    && EnchantmentHelper.isCompatible(EnchantmentHelper.get(other).keySet(), entry.getKey())) {
                other.addEnchantment(entry.getKey(), entry.getValue());
                used = true;
            }
        }

        if (used) {
            if (player.getWorld().isClient()) {
                for (int i = 0; i < 20; i++) {
                    float sin = (float) Math.sin(i * Math.PI / 10);
                    float cos = (float) Math.cos(i * Math.PI / 10);

                    player.getWorld().addParticle(ParticleTypes.END_ROD,
                            player.getX() + sin, player.getEyeY() - 0.5, player.getZ() + cos,
                            0, 0, 0);
                }
            }

            player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);

            stack.decrement(1);
            cir.setReturnValue(true);
        }
    }
}
