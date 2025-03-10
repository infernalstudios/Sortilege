package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.StaffItem;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;

    @Shadow @Nullable private String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    public void noAnvilCost(CallbackInfo ci) {
        ItemStack base = this.input.getStack(0);
        ItemStack addition = this.input.getStack(1);

        if (base.getItem() instanceof StaffItem && addition.getItem() instanceof DyeItem dye) {
            int i = 1;
            ItemStack result = base.copy();
            StaffItem.setBeamColor(result, dye.getColor().getColorComponents());

            if (this.newItemName != null && !Util.isBlank(this.newItemName)) {
                if (!this.newItemName.equals(base.getName().getString())) {
                    i++;
                    result.setCustomName(Text.literal(this.newItemName));
                }
            } else if (base.hasCustomName()) {
                i++;
                result.removeCustomName();
            }

            this.levelCost.set(i);
            this.output.setStack(0, result);
            this.sendContentUpdates();
        }

        if (ConfigEntries.noXPAnvil) this.levelCost.set(0);
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    public void canTakeFix(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigEntries.noXPAnvil) cir.setReturnValue(true);
    }
}
