package net.lyof.sortilege.item.custom;

import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class LimititeItem extends Item {
    public LimititeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ModConfig.limititeHasGlint.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, context, list, flag);

        if (Screen.hasShiftDown())
            for (String s : Component.translatable("item.sortilege.limitite.desc").getString().split("\n"))
                list.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
        else
            list.add(EnchantHelper.getShiftTooltip());
    }
}
