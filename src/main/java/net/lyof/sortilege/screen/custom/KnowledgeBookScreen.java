package net.lyof.sortilege.screen.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.mixin.accessor.ScreenAccessor;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.widget.AuthorsListWidget;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;
import java.util.Map;

public class KnowledgeBookScreen extends AbstractContainerScreen<KnowledgeBookScreenHandler> {
    public static final ResourceLocation BOOK_TEXTURE = Sortilege.MOD.makeID("textures/gui/knowledge_book.png");

    private int xoffset;
    private int pageIndex;
    private final int pageCount;
    private PageButton nextPageButton;
    private PageButton previousPageButton;
    private AuthorsListWidget authorsList;

    private int previousPageIndex;
    private List<FormattedCharSequence> pageCache;
    private ItemStack bookCache;
    private Enchantment enchantCache;

    public KnowledgeBookScreen(KnowledgeBookScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 192;
        this.pageIndex = 0;
        this.pageCount = KnowledgeBookItem.getKnowledge(handler.stack).getEntries().size();

        this.previousPageIndex = -1;
    }

    @Override
    protected void init() {
        super.init();

        this.xoffset = (this.width - 192) / 2 - this.leftPos;
        this.nextPageButton = this.addRenderableWidget(new PageButton(this.xoffset + 116 + this.leftPos, this.topPos + 159, true,
                button -> this.goToNextPage(), true));
        this.previousPageButton = this.addRenderableWidget(new PageButton(this.xoffset + 43 + this.leftPos, this.topPos + 159, false,
                button -> this.goToPreviousPage(), true));
        this.updatePageButtons();

        this.authorsList = new AuthorsListWidget(this.leftPos + this.xoffset + 36, this.topPos + 57, 104, 90,
                this.font, KnowledgeBookItem.getAuthors(this.menu.stack));
    }

    @Override
    public void onClose() {
        List<String> authors = this.authorsList.validate();
        KnowledgeBookItem.setAuthors(this.menu.stack, authors);

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeItem(this.menu.stack);
        ClientPlayNetworking.send(ModPackets.SET_KNOWLEDGE_AUTHORS, buf);

        super.onClose();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.authorsList.tick();
    }

    protected void goToPreviousPage() {
        if (this.pageIndex > 0)
            --this.pageIndex;

        this.updatePageButtons();
    }

    protected void goToNextPage() {
        if (this.pageIndex < this.pageCount)
            ++this.pageIndex;

        this.updatePageButtons();
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < this.pageCount;
        this.previousPageButton.visible = this.pageIndex > 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {  // Escape
            this.minecraft.player.closeContainer();
            return true;
        } if (keyCode == 257 && this.pageIndex == 0) {  // Enter
            List<String> authors = this.authorsList.validate();
            KnowledgeBookItem.setAuthors(this.menu.stack, authors);

            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeItem(this.menu.stack);
            ClientPlayNetworking.send(ModPackets.SET_KNOWLEDGE_AUTHORS, buf);
            return true;
        }

        if (this.authorsList.isActive())
            return this.authorsList.keyPressed(keyCode, scanCode, modifiers);

        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        else {
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.authorsList.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.authorsList.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.authorsList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.authorsList.mouseScrolled(mouseX, mouseY, amount) || super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.authorsList.isActive())
            return this.authorsList.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    public void setFocused(ItemStack stack) {
        this.menu.inventory.setItem(0, stack);
        this.hoveredSlot = this.menu.getSlot(0);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(BOOK_TEXTURE, this.xoffset, 2, 0, 0, 192, 192);
    }

    public void drawAuthorsPage(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float scale = 1.4f;

        context.pose().pushPose();
        context.pose().scale(scale, scale, 1);

        context.drawString(this.font, Component.translatable("tooltip.sortilege.knowledge_book.authors").withStyle(ChatFormatting.BOLD),
                (int) ((this.leftPos + this.xoffset + 36)/scale) + 1, (int) ((this.topPos + 40)/scale), 0xCCB998, false);

        context.pose().popPose();

        this.authorsList.render(context, mouseX, mouseY, delta);
    }

    public void drawPage(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Build page cache if it changed
        if (this.pageIndex != this.previousPageIndex) {
            EnchantKnowledge knowledge = KnowledgeBookItem.getKnowledge(this.menu.stack);
            Map.Entry<Enchantment, Integer> current = (Map.Entry<Enchantment, Integer>) knowledge.getEntries().toArray()[this.pageIndex - 1];

            MutableComponent content = Component.empty()
                    .append(((MutableComponent) current.getKey().getFullname(current.getValue())).withStyle(ChatFormatting.BLACK));
            MutableComponent desc = Component.translatableWithFallback(current.getKey().getDescriptionId() + ".desc", "");
            content.append("\n").append(desc.withStyle(ChatFormatting.GRAY));
            if (!desc.getString().isEmpty()) content.append("\n");

            this.pageCache = this.font.split(content, 114);
            this.bookCache = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(current.getKey(), current.getValue()));
            this.enchantCache = current.getKey();
            this.previousPageIndex = this.pageIndex;
        }

        int l = Math.min(128 / 9, this.pageCache.size());

        for (int i = 0; i < l; i++)
            context.drawString(this.font, this.pageCache.get(i), this.xoffset + 36, 42 + i * 9, 0, false);

        // Book display
        context.blit(BOOK_TEXTURE, this.xoffset + 36, 18, 48, 192, 20, 20);
        context.renderItem(this.bookCache, this.xoffset + 38, 20);
        if (this.isHovering(this.xoffset + 38, 20, 16, 16, mouseX, mouseY))
            this.setFocused(this.bookCache);

        float scale = 0.8f;

        context.pose().pushPose();
        context.pose().scale(scale, scale, 1);

        int i = 0; int j = 0;
        for (ItemStack stack : EnchantHelper.getCompatibleStacks(this.enchantCache)) {
            context.renderItem(stack, (int) ((this.xoffset + 41)/scale) + i*16, (int) ((40 + l*9)/scale) + j*16);
            if (this.isHovering((int) (this.xoffset + 41 + i*16*scale), (int) (40 + l*9 + j*16*scale),
                    (int) (16*scale), (int) (16*scale), mouseX, mouseY))
                this.setFocused(stack);

            i++;
            if (i > 114/scale/16 - 1) {
                j++;
                i = 0;
            } if (42 + l*9 + j*16*scale > 130 && i > 114/scale/16 - 4) {
                context.blit(BOOK_TEXTURE, (int) ((this.xoffset + 41)/scale) + i*16, (int) ((40 + l*9)/scale) + j*16,
                        0, 192, 48, 16);
                break;
            }
        }

        context.pose().popPose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.pose().pushPose();
        context.pose().translate(this.leftPos, this.topPos, 0);

        this.renderBg(context, delta, mouseX, mouseY);

        // Page X of X
        Component pageIndexText = Component.translatable("book.pageIndicator", this.pageIndex + 1, Math.max(this.pageCount + 1, 1));
        int k = this.font.width(pageIndexText);
        context.drawString(this.font, pageIndexText, this.xoffset - k + 192 - 44, 18, 0, false);

        this.hoveredSlot = null;
        this.authorsList.setVisible(this.pageIndex == 0);

        if (this.pageIndex != 0) this.drawPage(context, mouseX, mouseY, delta);

        context.pose().popPose();

        if (this.pageIndex == 0) this.drawAuthorsPage(context, mouseX, mouseY, delta);

        // Me, forgetting to call super? Never
        for (Renderable drawable : ((ScreenAccessor) this).getRenderables())
            drawable.render(context, mouseX, mouseY, delta);

        this.renderTooltip(context, mouseX, mouseY);
    }
}
