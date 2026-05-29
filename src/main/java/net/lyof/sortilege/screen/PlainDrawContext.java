package net.lyof.sortilege.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PlainDrawContext extends DrawContext {
    public PlainDrawContext(DrawContext parent) {
        super(MinecraftClient.getInstance(), parent.getVertexConsumers());
    }

    @Override
    public int drawTextWithShadow(TextRenderer textRenderer, @Nullable String text, int x, int y, int color) {
        return super.drawText(textRenderer, text, x, y, color, false);
    }

    @Override
    public int drawTextWithShadow(TextRenderer textRenderer, Text text, int x, int y, int color) {
        return super.drawText(textRenderer, text, x, y, color, false);
    }

    @Override
    public int drawTextWithShadow(TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        return super.drawText(textRenderer, text, x, y, color, false);
    }
}
