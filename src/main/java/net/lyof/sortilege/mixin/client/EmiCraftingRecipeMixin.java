package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiCraftingRecipe.class, remap = false)
public class EmiCraftingRecipeMixin {
    @Shadow @Final protected Identifier id;

    @Inject(method = "addWidgets", at = @At("HEAD"))
    public void addLockInfo(WidgetHolder widgets, CallbackInfo ci) {
        RecipeLock lock = RecipeLock.get(this.id.toString());
        if (lock == RecipeLock.NONE) return;

        widgets.addText(Text.translatable("sortilege.crafting.emi.lock"), 0, 60, 0xffffff, true);
        widgets.addText(lock.getFailMessage(), 4, 68, 0xffff00, false);
    }

    @ModifyReturnValue(method = "getDisplayHeight", at = @At("RETURN"))
    public int addLockHeight(int original) {
        if (RecipeLock.get(this.id.toString()) != RecipeLock.NONE)
            original += 20;
        return original;
    }
}
