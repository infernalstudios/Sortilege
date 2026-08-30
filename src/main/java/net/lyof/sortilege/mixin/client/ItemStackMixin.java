package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique private static Player sorti_player;
    @Unique private static ItemStack sorti_stack;

    @WrapOperation(method = "addAttributeTooltips", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"
    ))
    private void showStaffAttributes(ItemStack instance, EquipmentSlotGroup slot,
                                     BiConsumer<Holder<Attribute>, AttributeModifier> action, Operation<Void> original,
                                     Consumer<Component> tooltip, @Local MutableBoolean flag) {
        ItemStack self = (ItemStack) (Object) this;
        Player player = Minecraft.getInstance().player;

        if (self.getItem() instanceof AStaffItem staff && staff.shouldDisplayAttributes(self, player) && slot == EquipmentSlotGroup.HAND) {
            tooltip.accept(CommonComponents.EMPTY);
            tooltip.accept(Component.translatable("item.modifiers." + slot.getSerializedName()).withStyle(ChatFormatting.GRAY));
            flag.setFalse();

            String key = "attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id();
            DecimalFormat format = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT;

            float damage = staff.getDamage(self, player);
            float piercing = staff.getPiercing(self, player);
            float range = staff.getRange(self, player);

            if (damage > 0)
                tooltip.accept(CommonComponents.space().append(Component.translatable(key, format.format(damage),
                        Component.translatable(ModAttributes.STAFF_DAMAGE.value().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            if (piercing > 0)
                tooltip.accept(CommonComponents.space().append(Component.translatable(key, format.format(piercing),
                        Component.translatable(ModAttributes.STAFF_PIERCE.value().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
            if (range > 0)
                tooltip.accept(CommonComponents.space().append(Component.translatable(key, format.format(range),
                        Component.translatable(ModAttributes.STAFF_RANGE.value().getDescriptionId()))).withStyle(ChatFormatting.BLUE));

            original.call(instance, slot, (BiConsumer<Holder<Attribute>, AttributeModifier>) (attribute, modifier) -> {
                if (attribute == ModAttributes.STAFF_DAMAGE || attribute == ModAttributes.STAFF_PIERCE || attribute == ModAttributes.STAFF_RANGE)
                    return;
                action.accept(attribute, modifier);
            });
        } else original.call(instance, slot, action);
    }

    @Inject(method = "getTooltipLines", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"
    ))
    public void showCatalyst(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> list) {
        ItemStack self = (ItemStack) (Object) this;

        if (ModConfig.catalystTooltip.get() && EnchantingCatalyst.isCatalyst(self) && !(self.getItem() instanceof EnchantedBookItem)) {
            if (Screen.hasShiftDown()) {
                if (list.size() > 1 && !"".equals(list.get(list.size() - 1).getString()))
                    list.add(Component.empty());

                list.add(Component.translatable("tooltip.sortilege.catalyst").withStyle(ChatFormatting.DARK_PURPLE));

                for (Holder<Enchantment> e : EnchantingCatalyst.getEnchantments(self).keySet()) {
                    MutableComponent text = CommonComponents.space().append(e.value().description());
                    if (e.is(EnchantmentTags.CURSE))
                        text.withStyle(ChatFormatting.RED);
                    else
                        text.withStyle(ChatFormatting.GRAY);
                    list.add(text);
                }
            } else list.add(EnchantHelper.getShiftTooltip());
        }
    }

    @Inject(method = "getTooltipLines", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
            ordinal = 2
    ))
    private void showEnchantLimit(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> list) {
        if (player == null) return;
        ItemStack self = (ItemStack) (Object) this;

        if (self.getItem().getEnchantmentValue() > 0 && !self.is(Items.ENCHANTED_BOOK)) {
            int a = EnchantHelper.getUsedEnchantSlots(self);
            int m = EnchantHelper.getTotalEnchantSlots(self);

            if ((a > 0 || EnchantHelper.getExtraEnchantSlots(self) > 0 || ModConfig.alwaysShowEnchantLimit.get()) && m > 0) {

                MutableComponent txt = Component.translatableWithFallback("tooltip.sortilege.enchantments.limit." + a + "." + m,
                        a + "/" + m + " " + Component.translatable("tooltip.sortilege.enchantments").getString());

                if (list.size() > 1 && !list.get(list.size() - 1).getString().isEmpty() && !(self.getItem()instanceof AStaffItem))
                    list.add(Component.empty());
                list.add(txt.withStyle(a >= m ? ChatFormatting.RED : ChatFormatting.WHITE));
            }
        }

        sorti_player = player;
        sorti_stack = self;
    }

    @WrapOperation(method = "getTooltipLines", at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            //shift = At.Shift.AFTER,
            ordinal = 3
    ))
    private <E> boolean showStaffType(List<E> instance, E e, Operation<Boolean> original) {
        ItemStack self = (ItemStack) (Object) this;

        original.call(instance, e);
        if (self.getItem() instanceof AStaffItem staff)
            original.call(instance, Component.literal(" (" + staff.getEntry().getReader().getType() + ")").withStyle(ChatFormatting.DARK_GRAY));
        return true;
    }

/* TODO: move to ItemEnchantmentsMixin

    @WrapOperation(method = "appendEnchantmentNames", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private static void showLearnable(Optional<Enchantment> instance, Consumer<? super Enchantment> action, Operation<Void> original,
                                      List<Component> tooltip) {
        original.call(instance, (Consumer<? super Enchantment>) e -> {
            action.accept(e);

            if (ModConfig.knowledgeTooltip.get() && sorti_player instanceof EnchantLearner learner && sorti_stack != null
                    && learner.sorti_getKnowledge(sorti_stack).isLearnable(sorti_stack, e, EnchantHelper.getEnchantLevel(e, sorti_stack))) {

                Component text = Component.empty().append(tooltip.get(tooltip.size() - 1))
                        .append(Component.translatable("tooltip.sortilege.learnable").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.set(tooltip.size() - 1, text);
            }
        });
    }*/
}
