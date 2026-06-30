package net.lyof.sortilege.mixin.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique private static Player sorti_player;
    @Unique private static ItemStack sorti_stack;

    @WrapOperation(method = "getTooltipLines", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"
    ))
    public Multimap<Attribute, AttributeModifier> setStaffAttributes(ItemStack stack, EquipmentSlot slot, Operation<Multimap<Attribute, AttributeModifier>> original) {
        Multimap<Attribute, AttributeModifier> map = original.call(stack, slot);

        if (slot == EquipmentSlot.MAINHAND && stack.getItem() instanceof AStaffItem) {
            map = HashMultimap.create(map);
            map.put(ModAttributes.STAFF_DAMAGE, new AttributeModifier(ModAttributes.MARKER_UUID,
                    "Staff marker", 0, AttributeModifier.Operation.ADDITION));
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "getTooltipLines", at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"
    ))
    public <K, V> V showStaffAttributes(Map.Entry<K, V> instance, Operation<V> original, Player player, @Local List<Component> list) {
        V value = original.call(instance);
        ItemStack self = (ItemStack) (Object) this;

        if (self.getItem() instanceof AStaffItem staff && value instanceof AttributeModifier modifier) {
            if (player != null && modifier.getId().equals(ModAttributes.MARKER_UUID)) {
                String key = "attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue();
                DecimalFormat format = ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

                list.add(CommonComponents.space().append(Component.translatable(key, format.format(staff.getDamage(self, player)),
                        Component.translatable(ModAttributes.STAFF_DAMAGE.getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                list.add(CommonComponents.space().append(Component.translatable(key, format.format(staff.getPiercing(self, player)),
                        Component.translatable(ModAttributes.STAFF_PIERCE.getDescriptionId()))).withStyle(ChatFormatting.BLUE));
                list.add(CommonComponents.space().append(Component.translatable(key, format.format(staff.getRange(self, player)),
                        Component.translatable(ModAttributes.STAFF_RANGE.getDescriptionId()))).withStyle(ChatFormatting.BLUE));

                return value;
            }

            K key = instance.getKey();
            if (key instanceof Attribute attribute) {
                if (attribute == ModAttributes.STAFF_DAMAGE || attribute == ModAttributes.STAFF_PIERCE || attribute == ModAttributes.STAFF_RANGE)
                    return (V) new AttributeModifier(modifier.getId(), modifier.getName() + " override", 0, modifier.getOperation());
            }
        }

        return value;
    }

    @Inject(method = "getTooltipLines", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"
    ))
    public void showCatalyst(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir,
                             @Local List<Component> list) {
        ItemStack self = (ItemStack) (Object) this;

        if (ModConfig.catalystTooltip.get() && EnchantingCatalyst.isCatalyst(self) && !(self.getItem() instanceof EnchantedBookItem)) {
            if (Screen.hasShiftDown()) {
                if (list.size() > 1 && !"".equals(list.get(list.size() - 1).getString()))
                    list.add(Component.empty());

                list.add(Component.translatable("tooltip.sortilege.catalyst").withStyle(ChatFormatting.DARK_PURPLE));

                for (Enchantment e : EnchantingCatalyst.getEnchantments(self).keySet()) {
                    MutableComponent text = Component.translatable(e.getDescriptionId());
                    if (e.isCurse())
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
            target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"
    ))
    private void showEnchantLimit(@Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir,
                                  @Local List<Component> list) {
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
            ordinal = 16
    ))
    private <E> boolean showStaffType(List<E> instance, E e, Operation<Boolean> original) {
        ItemStack self = (ItemStack) (Object) this;

        original.call(instance, e);
        if (self.getItem() instanceof AStaffItem staff)
            original.call(instance, Component.literal(" (" + staff.getEntry().getReader().getType() + ")").withStyle(ChatFormatting.DARK_GRAY));
        return true;
    }

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
    }
}
