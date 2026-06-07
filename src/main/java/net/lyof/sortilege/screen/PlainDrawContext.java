package net.lyof.sortilege.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class PlainDrawContext extends GuiGraphics {
    public PlainDrawContext(GuiGraphics parent) {
        super(Minecraft.getInstance(), parent.bufferSource());
    }

    @Override
    public int drawString(Font textRenderer, @Nullable String text, int x, int y, int color) {
        return super.drawString(textRenderer, text, x, y, color, false);
    }

    @Override
    public int drawString(Font textRenderer, Component text, int x, int y, int color) {
        return super.drawString(textRenderer, text, x, y, color, false);
    }

    @Override
    public int drawString(Font textRenderer, FormattedCharSequence text, int x, int y, int color) {
        return super.drawString(textRenderer, text, x, y, color, false);
    }
}
