package net.lyof.sortilege.screen.widget;

import net.lyof.sortilege.Sortilege;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorsListWidget extends ScrollableWidget {
    public static final int BUTTON_SIZE = 12;

    private final TextRenderer textRenderer;
    private final List<TextFieldWidget> widgets;

    public AuthorsListWidget(int x, int y, int width, int height, TextRenderer textRenderer, List<String> authors) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.widgets = new ArrayList<>();
        this.build(authors);
    }

    public void build(List<String> authors) {
        int y = this.getY() + 1;
        this.widgets.clear();

        for (String author : authors) {
            TextFieldWidget widget = new TextFieldWidget(this.textRenderer, this.getX() + 1, y, width - 2, BUTTON_SIZE, Text.empty()) {
                @Override
                public int getY() {
                    return super.getY() - (int) AuthorsListWidget.this.getScrollY();
                }
            };
            widget.setFocusUnlocked(true);
            widget.setEditableColor(-1);
            widget.setUneditableColor(-1);
            widget.setText(author);

            this.widgets.add(widget);
            y += BUTTON_SIZE;
        }

        TextFieldWidget widget = new TextFieldWidget(textRenderer, this.getX() + 1, y, width - 2, BUTTON_SIZE, Text.empty()) {
            @Override
            public int getY() {
                return super.getY() - (int) AuthorsListWidget.this.getScrollY();
            }
        };
        widget.setFocusUnlocked(true);
        widget.setEditableColor(-1);
        widget.setUneditableColor(-1);
        widget.setText("");
        widget.setFocused(true);

        this.widgets.add(widget);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 9;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}


    @Override
    protected int getContentsHeight() {
        return this.widgets.size() * BUTTON_SIZE;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            //this.drawBox(drawContext);
            drawContext.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
            this.renderContents(drawContext, mouseX, mouseY, partialTick);
            drawContext.disableScissor();
            this.renderOverlay(drawContext);
        }
    }

    @Override
    protected void renderContents(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        for (TextFieldWidget widget : this.widgets)
            widget.render(drawContext, mouseX, mouseY, partialTicks);
    }

    public void tick() {
        for (TextFieldWidget widget : this.widgets)
            widget.tick();
    }

    public boolean isActive() {
        for (TextFieldWidget widget : this.widgets)
            if (widget.isActive()) return true;
        return false;
    }

    public void setVisible(boolean visible) {
        for (TextFieldWidget widget : this.widgets)
            widget.setVisible(visible);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean r = super.mouseClicked(mouseX, mouseY, button);
        if (!r) return false;
        if (!this.isWithinBounds(mouseX, mouseY)) {
            this.setFocused(true);
            return true;
        }

        this.setFocused(false);
        for (TextFieldWidget widget : this.widgets) {
            widget.setFocused(false);
            if (widget.isMouseOver(mouseX, mouseY)) {
                widget.setFocused(true);
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Sortilege.log(keyCode);
        boolean r = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 265) {  // Up
            Optional<TextFieldWidget> w = this.widgets.stream().filter(TextFieldWidget::isActive).findFirst();
            if (w.isPresent()) {
                int i = this.widgets.indexOf(w.get()) - 1;
                if (i >= 0) {
                    this.widgets.get(i).setFocused(true);
                    this.widgets.get(i + 1).setFocused(false);
                }
                return true;
            }
            return r;
        }
        if (keyCode == 264) {  // Down
            Optional<TextFieldWidget> w = this.widgets.stream().filter(TextFieldWidget::isActive).findFirst();
            if (w.isPresent()) {
                int i = this.widgets.indexOf(w.get()) + 1;
                if (i < this.widgets.size()) {
                    this.widgets.get(i).setFocused(true);
                    this.widgets.get(i - 1).setFocused(false);
                }
                return true;
            }
            return r;
        }

        for (TextFieldWidget widget : this.widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return r;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers))
            return true;

        for (TextFieldWidget widget : this.widgets) {
            if (widget.charTyped(chr, modifiers))
                return true;
        }
        return false;
    }

    public List<String> validate() {
        List<String> authors = new ArrayList<>();

        for (TextFieldWidget widget : this.widgets) {
            if (!widget.getText().isEmpty())
                authors.add(widget.getText());
        }
        String user = MinecraftClient.getInstance().player.getEntityName();
        if (!authors.contains(user))
            authors.add(user);

        this.build(authors);
        return authors;
    }
}