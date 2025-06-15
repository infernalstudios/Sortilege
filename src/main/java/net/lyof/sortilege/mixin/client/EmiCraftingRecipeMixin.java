package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiCraftingRecipe.class, remap = false)
public abstract class EmiCraftingRecipeMixin {
    @Unique private static final Identifier LOCK_BUTTON = Sortilege.makeID("textures/gui/emi/lock_button.png");

    @Shadow @Final protected Identifier id;
    @Shadow public abstract int getDisplayHeight();

    @Inject(method = "addWidgets", at = @At("TAIL"))
    public void addLockInfo(WidgetHolder widgets, CallbackInfo ci) {
        if (this.id == null) return;
        RecipeLock lock = RecipeLock.get(this.id.toString());
        if (lock == RecipeLock.NONE) return;

        // this.getDisplayWidth() + 5, this.getDisplayHeight() - 15*3 - 9
        widgets.add(new ButtonWidget(0, this.getDisplayHeight() - 12, 12, 12,
                0, 0, LOCK_BUTTON, () -> true, (x, y, index) -> {}) {
            @Override
            public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
                return List.of(TooltipComponent.of(lock.getFailMessage().asOrderedText()));
            }
        });
        widgets.addText(Text.translatable("sortilege.crafting.emi.locked_recipe"), 15, this.getDisplayHeight() - 9,
                0, false);
    }

    @ModifyReturnValue(method = "getDisplayHeight", at = @At("RETURN"))
    public int addLockHeight(int original) {
        if (this.id != null && RecipeLock.get(this.id.toString()) != RecipeLock.NONE)
            original += 13;
        return original;
    }
}
