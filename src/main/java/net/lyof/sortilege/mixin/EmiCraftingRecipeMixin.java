package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
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

        widgets.addText(lock.getFailMessage(), 10, 60, 0xffff00, false);
    }

    @ModifyReturnValue(method = "getDisplayHeight", at = @At("RETURN"))
    public int addLockHeight(int original) {
        if (RecipeLock.get(this.id.toString()) != RecipeLock.NONE)
            original += 20;
        return original;
    }
}
