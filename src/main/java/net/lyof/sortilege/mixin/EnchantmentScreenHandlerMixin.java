package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.crafting.EnchantingCatalyst;
import net.lyof.sortilege.util.IPropertyHolder;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler implements IPropertyHolder {
    @Shadow @Final private Inventory inventory;

    @Shadow @Final private ScreenHandlerContext context;

    @Shadow @Final private Random random;

    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Unique private final Inventory catalyst = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            EnchantmentScreenHandlerMixin.this.onContentChanged(EnchantmentScreenHandlerMixin.this.inventory);
        }
    };
    @Unique public final int[] catalyzed = new int[3];

    @Override
    public int getProperty(int i) {
        return this.catalyzed[i];
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(value = "TAIL"))
    public void allowOtherItems(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.addSlot(new Slot(this.catalyst, 0, 25, 20){
            @Override
            public boolean canInsert(ItemStack stack) {
                return EnchantingCatalyst.isCatalyst(stack.getItem());
            }

            @Override
            public boolean isEnabled() {
                ItemStack stack = EnchantmentScreenHandlerMixin.this.inventory.getStack(0);
                return !stack.isEmpty() && stack.isEnchantable();
            }
        });

        this.addProperty(Property.create(this.catalyzed, 0));
        this.addProperty(Property.create(this.catalyzed, 1));
        this.addProperty(Property.create(this.catalyzed, 2));
    }

    @Inject(method = "onClosed", at = @At("HEAD"))
    public void dropCatalyst(PlayerEntity player, CallbackInfo ci) {
        this.context.run((world, pos) -> this.dropInventory(player, this.catalyst));
    }

    @Inject(method = "generateEnchantments", at = @At("RETURN"), cancellable = true)
    public void applyCatalyst(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<Enchantment> catalyzed = EnchantingCatalyst.getEnchantments(this.catalyst.getStack(0).getItem());
        if (catalyzed.isEmpty()) {
            this.catalyzed[slot] = 0;
            return;
        }
        List<EnchantmentLevelEntry> result = new ArrayList<>();

        Enchantment chosen = MathHelper.randi(catalyzed.stream().filter(enchant -> enchant.isAcceptableItem(stack)).toList(), this.random);

        if (chosen == null || this.random.nextDouble() < 0.5) {
            this.catalyzed[slot] = 0;
            return;
        }

        this.catalyzed[slot] = 1;

        int a = this.random.nextInt(chosen.getMaxLevel()) + 1;
        int b = this.random.nextInt(chosen.getMaxLevel()) + 1;
        result.add(0, new EnchantmentLevelEntry(chosen, Math.min(a, b)));
        result.addAll(cir.getReturnValue().stream().filter(e -> e.enchantment.canCombine(chosen)).toList());

        cir.setReturnValue(result);
    }

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    public void useCatalyst(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            this.catalyst.getStack(0).decrement(1);
            this.catalyzed[0] = 0;
            this.catalyzed[1] = 0;
            this.catalyzed[2] = 0;
        }
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    public void moveCatalyst(PlayerEntity player, int slotid, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = this.getSlot(slotid);
        ItemStack stack = EnchantmentScreenHandlerMixin.this.inventory.getStack(0);

        if (slot.inventory == this.catalyst && !this.insertItem(slot.getStack(), 2, 38, true)) {
            slot.onTakeItem(player, slot.getStack());
            cir.setReturnValue(ItemStack.EMPTY);
        }
        else if (EnchantingCatalyst.isCatalyst(slot.getStack().getItem())
                && !stack.isEmpty() && stack.isEnchantable() && !this.insertItem(slot.getStack(), 38, 39, true)) {

            slot.onTakeItem(player, slot.getStack());
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    public void updateCatalystOverlay(Inventory inventory, CallbackInfo ci) {
        ItemStack stack = this.inventory.getStack(0);
        if (stack.isEmpty() || !stack.isEnchantable()) {
            this.catalyzed[0] = 0;
            this.catalyzed[1] = 0;
            this.catalyzed[2] = 0;
        }
    }
}
