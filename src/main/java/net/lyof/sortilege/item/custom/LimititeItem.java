package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.EnchantHelper;
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
    public LimititeItem(FabricItemSettings properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ModConfig.limititeHasGlint.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag context) {
        super.appendHoverText(stack, level, list, context);

        if (Screen.hasShiftDown())
            list.add(Component.translatable("item.sortilege.limitite.desc").withStyle(ChatFormatting.YELLOW));
        else
            list.add(EnchantHelper.getShiftTooltip());
    }
}
