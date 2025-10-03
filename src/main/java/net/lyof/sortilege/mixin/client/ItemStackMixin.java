package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.enchanting.EnchantingCatalyst;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Shadow public abstract Rarity getRarity();

    @Shadow public abstract boolean isIn(TagKey<Item> tag);

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V"))
    public void showEnchantLimit(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir,
                                 @Local List<Text> list) {
        ItemStack self = (ItemStack) (Object) this;

        if (ConfigEntries.catalystTooltip && EnchantingCatalyst.isCatalyst(self) && !(self.getItem() instanceof EnchantedBookItem)) {
            if (Screen.hasShiftDown()) {
                if (list.size() > 1 && !"".equals(list.get(list.size() - 1).getString()))
                    list.add(Text.empty());

                list.add(Text.translatable("item.sortilege.catalyst.desc").formatted(Formatting.DARK_PURPLE));

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

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;formatted(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;", ordinal = 0))
    private MutableText changeRarityFormatting(MutableText instance, Formatting formatting, Operation<MutableText> original) {
        if (this.isIn(ModTags.Items.FORGOTTEN_ITEMS))
            return instance.formatted(Formatting.GREEN);
        return original.call(instance, formatting);
    }
}
