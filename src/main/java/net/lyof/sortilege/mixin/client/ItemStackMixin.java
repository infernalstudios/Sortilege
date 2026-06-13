package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();
    @Shadow public abstract boolean is(TagKey<Item> tag);

    @Unique private static Player sorti_player;
    @Unique private static ItemStack sorti_stack;

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"))
    public void showCatalyst(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir,
                             @Local List<Component> list) {
        ItemStack self = (ItemStack) (Object) this;

        if (ModConfig.catalystTooltip.get() && EnchantingCatalyst.isCatalyst(self) && !(self.getItem() instanceof EnchantedBookItem)) {
            if (Screen.hasShiftDown()) {
                if (list.size() > 1 && !"".equals(list.get(list.size() - 1).getString()))
                    list.add(Component.empty());

                list.add(Component.translatable("item.sortilege.catalyst.desc").withStyle(ChatFormatting.DARK_PURPLE));

                for (Enchantment e : EnchantingCatalyst.getEnchantments(self).keySet()) {
                    MutableComponent text = Component.translatable(e.getDescriptionId());
                    if (e.isCurse())
                        text.withStyle(ChatFormatting.RED);
                    else
                        text.withStyle(ChatFormatting.GRAY);
                    list.add(text);
                }
            } else
                list.add(EnchantHelper.getShiftTooltip());
        }
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"))
    private void showEnchantLimit(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir,
                                 @Local List<Component> list) {
        ItemStack self = (ItemStack) (Object) this;

        if (self.getItem().getEnchantmentValue() > 0 && !self.is(Items.ENCHANTED_BOOK)) {
            int a = EnchantHelper.getUsedEnchantSlots(self);
            int m = EnchantHelper.getTotalEnchantSlots(self);

            if ((a > 0 || EnchantHelper.getExtraEnchantSlots(self) > 0 || ModConfig.alwaysShowEnchantLimit.get()) && m > 0) {

                MutableComponent txt = Component.translatableWithFallback("sortilege.enchantments.limit." + a + "." + m,
                        a + "/" + m + " " + Component.translatable("sortilege.enchantments").getString());

                if (list.size() > 1 && !"".equals(list.get(list.size() - 1).getString()))
                    list.add(Component.empty());
                list.add(txt.withStyle(a >= m ? ChatFormatting.RED : ChatFormatting.WHITE));
            }
        }

        sorti_player = player;
        sorti_stack = self;
    }

    @WrapOperation(method = "appendEnchantmentNames", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private static void showLearnable(Optional<Enchantment> instance, Consumer<? super Enchantment> action, Operation<Void> original,
                                      List<Component> tooltip) {
        original.call(instance, (Consumer<? super Enchantment>) e -> {
            action.accept(e);

            if (ModConfig.knowledgeTooltip.get() && sorti_player instanceof EnchantLearner learner && sorti_stack != null
                    && learner.sorti_getKnowledge(sorti_stack).isLearnable(sorti_stack, e, EnchantHelper.getEnchantLevel(e, sorti_stack))) {

                Component text = Component.empty().append(tooltip.get(tooltip.size() - 1))
                        .append(Component.translatable("item.sortilege.learnable.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.set(tooltip.size() - 1, text);
            }
        });
    }

    @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private MutableComponent changeRarityFormatting(MutableComponent instance, ChatFormatting formatting, Operation<MutableComponent> original) {
        if (this.is(ModTags.Items.FORGOTTEN_ITEMS))
            return instance.withStyle(ChatFormatting.GREEN);
        return original.call(instance, formatting);
    }
}
