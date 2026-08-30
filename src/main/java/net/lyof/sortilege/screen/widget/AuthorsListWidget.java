package net.lyof.sortilege.screen.widget;

import net.lyof.sortilege.mixin.accessor.EditBoxAccessor;
import net.lyof.sortilege.screen.PlainDrawContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorsListWidget extends AbstractScrollWidget {
    public static final int BUTTON_SIZE = 12;

    private final Font textRenderer;
    private final List<EditBox> widgets;

    public AuthorsListWidget(int x, int y, int width, int height, Font textRenderer, List<String> authors) {
        super(x, y, width, height, Component.empty());
        this.textRenderer = textRenderer;
        this.widgets = new ArrayList<>();
        this.build(authors);
    }

    public void build(List<String> authors) {
        int y = this.getY() + 1;
        this.widgets.clear();

        for (String author : authors) {
            this.widgets.add(makeTextField(author, y));

            y += BUTTON_SIZE;
        }

        EditBox widget = makeTextField("", y);
        widget.setFocused(true);

        this.widgets.add(widget);
    }

    private @NotNull EditBox makeTextField(String author, int y) {
        EditBox widget = new EditBox(this.textRenderer, this.getX() + 1, y, width - 2, BUTTON_SIZE, Component.empty()) {
            @Override
            public int getY() {
                return super.getY() - (int) AuthorsListWidget.this.scrollAmount();
            }
        };
        widget.setCanLoseFocus(true);
        widget.setBordered(false);
        widget.setTextColor(ChatFormatting.DARK_GRAY.getColor());
        widget.setTextColorUneditable(ChatFormatting.GRAY.getColor());
        widget.setValue(author);
        if (y == this.getY() + 1 || author.equals(Minecraft.getInstance().player.getScoreboardName()))
            widget.setEditable(false);
        return widget;
    }

    @Override
    protected double scrollRate() {
        return 9;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}


    @Override
    protected int getInnerHeight() {
        return this.widgets.size() * BUTTON_SIZE;
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            //this.drawBox(drawContext);
            drawContext.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
            this.renderContents(drawContext, mouseX, mouseY, partialTick);
            drawContext.disableScissor();
            this.renderDecorations(drawContext);
        }
    }

    @Override
    protected void renderContents(GuiGraphics drawContext, int mouseX, int mouseY, float partialTicks) {
        drawContext = new PlainDrawContext(drawContext);
        for (EditBox widget : this.widgets)
            widget.render(drawContext, mouseX, mouseY, partialTicks);
    }

    public boolean isActive() {
        for (EditBox widget : this.widgets)
            if (widget.canConsumeInput()) return true;
        return false;
    }

    public void setVisible(boolean visible) {
        for (EditBox widget : this.widgets)
            widget.setVisible(visible);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean r = super.mouseClicked(mouseX, mouseY, button);
        if (r && !this.withinContentAreaPoint(mouseX, mouseY)) {
            this.setFocused(true);
            return true;
        }

        this.setFocused(false);
        for (EditBox widget : this.widgets) {
            widget.setFocused(false);
            if (((EditBoxAccessor) widget).isEditable() && widget.isMouseOver(mouseX, mouseY)) {
                widget.setFocused(true);
                r = widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return r;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean r = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 265) {  // Up
            Optional<EditBox> w = this.widgets.stream().filter(EditBox::canConsumeInput).findFirst();
            if (w.isPresent()) {
                int i = this.widgets.indexOf(w.get()) - 1;
                while (i > 0 && !((EditBoxAccessor) this.widgets.get(i)).isEditable())
                    i--;
                while (i < 0 || !((EditBoxAccessor) this.widgets.get(i)).isEditable())
                    i++;

                w.get().setFocused(false);
                this.widgets.get(i).setFocused(true);
                return true;
            }
            return r;
        }
        if (keyCode == 264) {  // Down
            Optional<EditBox> w = this.widgets.stream().filter(EditBox::canConsumeInput).findFirst();
            if (w.isPresent()) {
                int i = this.widgets.indexOf(w.get()) + 1;
                while (i < this.widgets.size() && !((EditBoxAccessor) this.widgets.get(i)).isEditable())
                    i++;
                while (i >= this.widgets.size() || !((EditBoxAccessor) this.widgets.get(i)).isEditable())
                    i--;

                w.get().setFocused(false);
                this.widgets.get(i).setFocused(true);
                return true;
            }
            return r;
        }

        for (EditBox widget : this.widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return r;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers))
            return true;

        for (EditBox widget : this.widgets) {
            if (widget.charTyped(chr, modifiers))
                return true;
        }
        return false;
    }

    public List<String> validate() {
        List<String> authors = new ArrayList<>();

        for (EditBox widget : this.widgets) {
            if (!widget.getValue().isEmpty())
                authors.add(widget.getValue());
        }
        /*String user = MinecraftClient.getInstance().player.getEntityName();
        if (!authors.contains(user))
            authors.add(user);*/

        this.build(authors);
        return authors;
    }
}