package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.crafting.EnchantmentCatalyst;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {
    @Shadow @Final private Inventory inventory;

    @Shadow @Final private ScreenHandlerContext context;

    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Unique private final Inventory catalyst = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            EnchantmentScreenHandlerMixin.this.onContentChanged(this);
        }
    };

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(value = "TAIL"))
    public void allowOtherItems(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.addSlot(new Slot(this.catalyst, 0, 25, 20){
            @Override
            public boolean canInsert(ItemStack stack) {
                return EnchantmentCatalyst.isCatalyst(stack.getItem());
            }

            @Override
            public boolean isEnabled() {
                return !EnchantmentScreenHandlerMixin.this.inventory.getStack(0).isEmpty();
            }
        });
    }

    @Inject(method = "onClosed", at = @At("HEAD"))
    public void dropCatalyst(PlayerEntity player, CallbackInfo ci) {
        this.context.run((world, pos) -> this.dropInventory(player, this.catalyst));
    }

    @Inject(method = "generateEnchantments", at = @At("HEAD"))
    public void applyCatalyst(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        Sortilege.log(this.catalyst.getStack(0));
    }
}
