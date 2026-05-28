package net.lyof.sortilege.screen.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.mixin.accessor.ScreenAccessor;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class KnowledgeBookScreen extends HandledScreen<KnowledgeBookScreenHandler> {
    public static final Identifier BOOK_TEXTURE = Sortilege.makeID("textures/gui/knowledge_book.png");

    private int xoffset;
    private int pageIndex;
    private final int pageCount;
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;

    private int previousPageIndex;
    private List<OrderedText> pageCache;
    private ItemStack bookCache;
    private Enchantment enchantCache;

    public KnowledgeBookScreen(KnowledgeBookScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 192;
        this.pageIndex = 0;
        this.pageCount = KnowledgeBookItem.getKnowledge(handler.stack).getEntries().size();

        this.previousPageIndex = -1;
    }

    @Override
    protected void init() {
        super.init();

        this.xoffset = (this.width - 192) / 2 - this.x;
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(this.xoffset + 116 + this.x, this.y + 159, true,
                button -> this.goToNextPage(), true));
        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(this.xoffset + 43 + this.x, this.y + 159, false,
                button -> this.goToPreviousPage(), true));
        this.updatePageButtons();
    }

    protected void goToPreviousPage() {
        if (this.pageIndex > 0)
            --this.pageIndex;

        this.updatePageButtons();
    }

    protected void goToNextPage() {
        if (this.pageIndex < this.pageCount - 1)
            ++this.pageIndex;

        this.updatePageButtons();
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < this.pageCount - 1;
        this.previousPageButton.visible = this.pageIndex > 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            return switch (keyCode) {
                case 266 -> {
                    this.previousPageButton.onPress();
                    yield true;
                }
                case 267 -> {
                    this.nextPageButton.onPress();
                    yield true;
                }
                default -> false;
            };
        }
    }

    public void setFocused(ItemStack stack) {
        this.handler.inventory.setStack(0, stack);
        this.focusedSlot = this.handler.getSlot(0);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BOOK_TEXTURE, this.xoffset, 2, 0, 0, 192, 192);
    }

    public void drawPage(DrawContext context, int mouseX, int mouseY, float delta) {
        // Build page cache if it changed
        if (this.pageIndex != this.previousPageIndex) {
            EnchantKnowledge knowledge = KnowledgeBookItem.getKnowledge(this.handler.stack);
            Map.Entry<Enchantment, Integer> current = (Map.Entry<Enchantment, Integer>) knowledge.getEntries().toArray()[this.pageIndex];

            MutableText content = Text.empty()
                    .append(((MutableText) current.getKey().getName(current.getValue())).formatted(Formatting.BLACK));
            MutableText desc = Text.translatableWithFallback(current.getKey().getTranslationKey() + ".desc", "");
            content.append("\n").append(desc.formatted(Formatting.GRAY));
            if (!desc.getString().isEmpty()) content.append("\n");

            this.pageCache = this.textRenderer.wrapLines(content, 114);
            this.bookCache = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(current.getKey(), current.getValue()));
            this.enchantCache = current.getKey();
            this.previousPageIndex = this.pageIndex;
        }

        int l = Math.min(128 / 9, this.pageCache.size());

        for (int i = 0; i < l; i++)
            context.drawText(this.textRenderer, this.pageCache.get(i), this.xoffset + 36, 42 + i * 9, 0, false);

        // Book display
        context.drawItem(this.bookCache, this.xoffset + 132, 32);
        if (this.isPointWithinBounds(this.xoffset + 132, 32, 16, 16, mouseX, mouseY))
            this.setFocused(this.bookCache);

        // Compatible items list
        float scale = 0.75f;

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1);

        int i = 0; int j = 0;
        for (ItemStack stack : ItemHelper.getCompatibleStacks(this.enchantCache)) {
            context.drawItem(stack, (int) ((this.xoffset + 36)/scale) + i*16, (int) ((42 + l*9)/scale) + j*16);
            if (this.isPointWithinBounds((int) (this.xoffset + 36 + i*16*scale), (int) (42 + l*9 + j*16*scale),
                    (int) (16*scale), (int) (16*scale), mouseX, mouseY))
                this.setFocused(stack);

            i++;
            if (i > 114/scale/16 - 1) {
                j++;
                i = 0;
            }
            if (j > (6 - l)/scale && i > 114/scale/16 - 4) {
                context.drawTexture(BOOK_TEXTURE, (int) ((this.xoffset + 36)/scale) + i*16, (int) ((42 + l*9)/scale) + j*16,
                        0, 192, 48, 16);
                break;
            }
        }

        context.getMatrices().pop();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0);

        this.drawBackground(context, delta, mouseX, mouseY);

        // Page X of X
        Text pageIndexText = Text.translatable("book.pageIndicator", this.pageIndex + 1, Math.max(this.pageCount, 1));
        int k = this.textRenderer.getWidth(pageIndexText);
        context.drawText(this.textRenderer, pageIndexText, this.xoffset - k + 192 - 44, 18, 0, false);

        this.focusedSlot = null;

        this.drawPage(context, mouseX, mouseY, delta);

        context.getMatrices().pop();

        // Me, forgetting to call super? Never
        for (Drawable drawable : ((ScreenAccessor) this).getDrawables())
            drawable.render(context, mouseX, mouseY, delta);

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
