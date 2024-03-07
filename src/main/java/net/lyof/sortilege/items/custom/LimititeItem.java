package net.lyof.sortilege.items.custom;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LimititeItem extends Item {
    public LimititeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ConfigEntries.isLimititeFoil;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);

        if (Screen.hasShiftDown())
            list.add(Component.translatable("sortilege.limitite.desc").withStyle(ChatFormatting.YELLOW));
        else
            list.add(ItemHelper.getShiftTooltip());
    }
}
