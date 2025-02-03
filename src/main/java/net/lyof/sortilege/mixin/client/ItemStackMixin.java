package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.crafting.EnchantingCatalyst;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltip", at = @At(value = "TAIL"))
    public void showEnchantLimit(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir,
                                 @Local List<Text> list) {
        ItemStack self = (ItemStack) (Object) this;

        if (ConfigEntries.catalystTooltip && EnchantingCatalyst.isCatalyst(self) && !(self.getItem() instanceof EnchantedBookItem)) {
            if (Screen.hasShiftDown()) {
                if (list.size() > 1)
                    list.add(Text.empty());

                list.add(Text.translatable("sortilege.catalyst").formatted(Formatting.DARK_PURPLE));

                for (Enchantment e : EnchantingCatalyst.getEnchantments(self).keySet()) {
                    MutableText text = Text.translatable(e.getTranslationKey());
                    if (e.isCursed())
                        text.formatted(Formatting.RED);
                    else
                        text.formatted(Formatting.GRAY);
                    list.add(text);
                }
            } else
                list.add(ItemHelper.getShiftTooltip());
        }
    }
}
