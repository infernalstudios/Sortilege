package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LimititeItem extends Item {
    public LimititeItem(FabricItemSettings properties) {
        super(properties);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return ConfigEntries.isLimititeFoil;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext context) {
        super.appendTooltip(stack, level, list, context);

        if (Screen.hasShiftDown())
            list.add(Text.translatable("sortilege.limitite.desc").formatted(Formatting.YELLOW));
        else
            list.add(ItemHelper.getShiftTooltip());
    }
}
