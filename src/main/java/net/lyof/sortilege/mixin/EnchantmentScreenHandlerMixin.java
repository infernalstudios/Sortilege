package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.inject.EnchantInfoHolder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import java.util.Map;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler implements EnchantInfoHolder {
    @Shadow @Final private Inventory inventory;
    @Shadow @Final private ScreenHandlerContext context;
    @Shadow @Final private Random random;

    @Shadow @Final public int[] enchantmentPower;

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
    public boolean sorti_isCatalyzed(int slot) {
        return this.catalyzed[slot] == 1;
    }

    @Override
    public boolean sorti_hasCatalyst() {
        return !this.catalyst.getStack(0).isEmpty();
    }

    @Override
    public boolean sorti_hasEnchantableItem() {
        ItemStack stack = this.inventory.getStack(0);
        return !stack.isEmpty() && stack.isEnchantable() && (ConfigEntries.bookCatalysts || !EnchantingCatalyst.isEmpty());
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(value = "TAIL"))
    public void addCatalystSlot(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        if (!ConfigEntries.bookCatalysts && EnchantingCatalyst.isEmpty())
            return;

        this.addSlot(new Slot(this.catalyst, 0, 25, 20){
            @Override
            public boolean canInsert(ItemStack stack) {
                return EnchantingCatalyst.isCatalyst(stack);
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

    @WrapOperation(method = "generateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;"))
    public List<EnchantmentLevelEntry> overrideDefaultEnchanting(Random random, ItemStack stack, int level, boolean treasureAllowed, Operation<List<EnchantmentLevelEntry>> original) {
        if (ConfigEntries.overrideDefaultEnchanting)
            return new ArrayList<>();
        return original.call(random, stack, level, treasureAllowed);
    }

    @ModifyReturnValue(method = "generateEnchantments", at = @At("RETURN"))
    public List<EnchantmentLevelEntry> applyCatalyst(List<EnchantmentLevelEntry> original, ItemStack stack, int slot, int level) {
        int power = this.enchantmentPower[slot];
        if (original.isEmpty())
            this.enchantmentPower[slot] = 0;
        this.catalyzed[slot] = 0;

        if (!ConfigEntries.bookCatalysts && EnchantingCatalyst.isEmpty())
            return original;

        Map<Enchantment, Integer> enchants = EnchantingCatalyst.getEnchantments(this.catalyst.getStack(0));
        if (enchants.isEmpty())
            return original;

        for (int i = 0; i < slot; i++)
            this.random.nextDouble();

        Enchantment chosen = MathHelper.randi(enchants.keySet().stream().filter(enchant -> enchant.isAcceptableItem(stack)
                        || stack.isOf(Items.BOOK)).toList(), this.random);

        if (chosen == null)
            return original;

        for (int i = 0; i < Registries.ENCHANTMENT.getRawId(chosen) % 8; i++)
            this.random.nextDouble();

        if (this.random.nextDouble() > ConfigEntries.catalystChance)
            return original;

        int lvl = chosen.getMaxLevel();
        for (int i = 0; i < 3 - slot; i++)
            lvl = Math.min(lvl, this.random.nextInt(chosen.getMaxLevel()));
        lvl += 1;
        if (this.catalyst.getStack(0).getItem() instanceof EnchantedBookItem)
            lvl = Math.min(lvl, enchants.get(chosen));

        List<EnchantmentLevelEntry> result = new ArrayList<>();

        for (EnchantmentLevelEntry entry : original) {
            if (entry.enchantment == chosen && entry.level > lvl)
                lvl = entry.level;
            else if (entry.enchantment.canCombine(chosen))
                result.add(entry);
        }
        result.add(0, new EnchantmentLevelEntry(chosen, lvl));

        this.catalyzed[slot] = 1;
        this.enchantmentPower[slot] = power;
        return result;
    }

    @WrapMethod(method = "onButtonClick")
    public boolean useCatalyst(PlayerEntity player, int id, Operation<Boolean> original) {
        boolean result = original.call(player, id);

        if (!ConfigEntries.bookCatalysts && EnchantingCatalyst.isEmpty())
            return result;

        if (result) {
            if (!(this.catalyst.getStack(0).getItem() instanceof EnchantedBookItem))
                this.catalyst.getStack(0).decrement(1);
            this.catalyzed[0] = 0;
            this.catalyzed[1] = 0;
            this.catalyzed[2] = 0;
        }
        return result;
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    public void moveCatalyst(PlayerEntity player, int slotid, CallbackInfoReturnable<ItemStack> cir) {
        if (!ConfigEntries.bookCatalysts && EnchantingCatalyst.isEmpty())
            return;


        Slot slot = this.getSlot(slotid);
        ItemStack stack = this.inventory.getStack(0);

        if (slot.inventory == this.catalyst && !this.insertItem(slot.getStack(), 2, 38, true)) {
            slot.onTakeItem(player, slot.getStack());
            this.catalyst.markDirty();
            cir.setReturnValue(ItemStack.EMPTY);
        }
        else if (EnchantingCatalyst.isCatalyst(slot.getStack())
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
