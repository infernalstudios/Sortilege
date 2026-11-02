package net.lyof.sortilege.mixin.client;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.function.BiConsumer;

@Mixin(AttributeUtil.class)
public class AttributeUtilMixin {
    @Unique private static final ResourceLocation STAFF_DAMAGE_ID = Sortilege.MOD.makeID("base_staff_damage");
    @Unique private static final ResourceLocation STAFF_PIERCE_ID = Sortilege.MOD.makeID("base_staff_pierce");
    @Unique private static final ResourceLocation STAFF_RANGE_ID = Sortilege.MOD.makeID("base_staff_range");

    @WrapOperation(method = "getSortedModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"))
    private static void showStaffAttributes(ItemStack self, EquipmentSlotGroup slot,
                                            BiConsumer<Holder<Attribute>, AttributeModifier> action, Operation<Void> original) {

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (self.getItem() instanceof AStaffItem staff && staff.shouldDisplayAttributes(self, player) && slot == EquipmentSlotGroup.HAND) {
            float damage = staff.getStaffDamage(self, player);
            float piercing = staff.getPiercing(self, player);
            float range = staff.getRange(self, player);

            if (damage > 0)
                action.accept(ModAttributes.STAFF_DAMAGE.getAsHolder(), new AttributeModifier(STAFF_DAMAGE_ID, damage,
                        AttributeModifier.Operation.ADD_VALUE));
            if (piercing > 0)
                action.accept(ModAttributes.STAFF_PIERCE.getAsHolder(), new AttributeModifier(STAFF_PIERCE_ID, piercing,
                        AttributeModifier.Operation.ADD_VALUE));
            if (range > 0)
                action.accept(ModAttributes.STAFF_RANGE.getAsHolder(), new AttributeModifier(STAFF_RANGE_ID, range,
                        AttributeModifier.Operation.ADD_VALUE));

            original.call(self, slot, (BiConsumer<Holder<Attribute>, AttributeModifier>) (attribute, modifier) -> {
                if (attribute == ModAttributes.STAFF_DAMAGE.getAsHolder() || attribute == ModAttributes.STAFF_PIERCE.getAsHolder() || attribute == ModAttributes.STAFF_RANGE.getAsHolder())
                    return;
                action.accept(attribute, modifier);
            });
        } else original.call(self, slot, action);
    }
}
