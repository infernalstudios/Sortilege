package net.lyof.sortilege.attribute.custom;

import net.lyof.sortilege.attribute.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.text.DecimalFormat;

public class StaffAttribute extends RangedAttribute {
    private static final String KEY = "attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id();
    private static final DecimalFormat FORMAT = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT;

    private final ChatFormatting formatting;

    public StaffAttribute(String desc, double defaultValue, double min, double max, ChatFormatting formatting) {
        super(desc, defaultValue, min, max);
        this.formatting = formatting;
    }

    @Override
    public MutableComponent toComponent(AttributeModifier modif, TooltipFlag flag) {
        return CommonComponents.space().append(Component.translatable(KEY, FORMAT.format(modif.amount()),
                Component.translatable(this.getDescriptionId()))).withStyle(this.formatting);
    }
}
