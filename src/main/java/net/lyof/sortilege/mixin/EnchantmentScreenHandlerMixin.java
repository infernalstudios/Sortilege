package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
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

    @Unique private final Inventory sorti_catalyst = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            EnchantmentScreenHandlerMixin.this.onContentChanged(EnchantmentScreenHandlerMixin.this.inventory);
        }
    };
    @Unique private final int[] sorti_catalyzed = new int[3];
    @Unique private PlayerEntity sorti_player = null;

    @Override
    public boolean sorti_isCatalyzed(int slot) {
        return this.sorti_catalyzed[slot] == 1;
    }

    @Override
    public boolean sorti_hasCatalyst() {
        return !this.sorti_catalyst.getStack(0).isEmpty();
    }

    @Override
    public boolean sorti_hasEnchantableItem() {
        ItemStack stack = this.inventory.getStack(0);
        return !stack.isEmpty() && stack.isEnchantable() && !EnchantingCatalyst.isDisabled();
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(value = "TAIL"))
    public void addCatalystSlot(int syncId, PlayerInventory inventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.sorti_player = inventory.player;

        if (EnchantingCatalyst.isDisabled())
            return;

        this.addSlot(new Slot(this.sorti_catalyst, 0, 25, 20){
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

        this.addProperty(Property.create(this.sorti_catalyzed, 0));
        this.addProperty(Property.create(this.sorti_catalyzed, 1));
        this.addProperty(Property.create(this.sorti_catalyzed, 2));
    }

    @Inject(method = "onClosed", at = @At("HEAD"))
    public void dropCatalyst(PlayerEntity player, CallbackInfo ci) {
        this.context.run((world, pos) -> this.dropInventory(player, this.sorti_catalyst));
    }

    @WrapOperation(method = "generateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;"))
    public List<EnchantmentLevelEntry> overrideDefaultEnchanting(Random random, ItemStack stack, int level, boolean treasureAllowed, Operation<List<EnchantmentLevelEntry>> original) {
        if (ConfigEntries.catalystOnly)
            return new ArrayList<>();
        return original.call(random, stack, level, treasureAllowed);
    }

    @ModifyReturnValue(method = "generateEnchantments", at = @At("RETURN"))
    public List<EnchantmentLevelEntry> generateSortiEnchantments(List<EnchantmentLevelEntry> original, ItemStack stack, int slot, int level) {
        int power = this.enchantmentPower[slot];
        this.sorti_catalyzed[slot] = 0;

        // We need a mutable list
        List<EnchantmentLevelEntry> result = new ArrayList<>(original);

        // Knowledge logic
        if (ConfigEntries.knowledgeEnabled && this.sorti_player != null) {
            EnchantKnowledge knowledge = ((EnchantLearner) this.sorti_player).sorti_getKnowledge(stack);

            result.removeIf(entry -> !knowledge.isKnown(entry.enchantment));
            List<EnchantmentLevelEntry> list = new ArrayList<>();
            for (EnchantmentLevelEntry entry : result)
                list.add(new EnchantmentLevelEntry(entry.enchantment,
                        Math.min(entry.level, knowledge.getKnownLevel(entry.enchantment))));
            result = list;
        }

        // Catalyst logic
        Map<Enchantment, Integer> enchants = EnchantingCatalyst.getEnchantments(this.sorti_catalyst.getStack(0));
        if (!enchants.isEmpty()) {
            // Randomize seed
            for (int i = 0; i < slot; i++) this.random.nextDouble();

            // Get catalyzed enchant
            Enchantment chosen = MathHelper.randi(enchants.keySet().stream().filter(enchant -> enchant.isAcceptableItem(stack)
                    || stack.isOf(Items.BOOK)).toList(), this.random);
            if (chosen == null) return original;

            // Randomize seed
            for (int i = 0; i < Registries.ENCHANTMENT.getRawId(chosen) % 8; i++) this.random.nextDouble();

            // Catalysts don't always apply
            if (this.random.nextDouble() <= ConfigEntries.catalystChance) {
                int lvl = chosen.getMaxLevel();
                // Higher level in costier slots
                for (int i = 0; i < 3 - slot; i++)
                    lvl = Math.min(lvl, this.random.nextInt(chosen.getMaxLevel()));
                lvl += 1;

                // Nerf books, because they are reusable
                if (this.sorti_catalyst.getStack(0).getItem() instanceof EnchantedBookItem)
                    lvl = Math.min(lvl, enchants.get(chosen));

                // Remove incompatible and duplicate enchants
                result.removeIf(entry -> !entry.enchantment.canCombine(chosen));
                result.add(0, new EnchantmentLevelEntry(chosen, lvl));

                // Slot was successfully catalyzed
                this.sorti_catalyzed[slot] = 1;
                this.enchantmentPower[slot] = power;
            }
        }

        if (result.isEmpty()) this.enchantmentPower[slot] = 0;
        return result;
    }

    @WrapMethod(method = "onButtonClick")
    public boolean useCatalyst(PlayerEntity player, int id, Operation<Boolean> original) {
        boolean result = original.call(player, id);

        if (EnchantingCatalyst.isDisabled())
            return result;

        if (result) {
            if (!(this.sorti_catalyst.getStack(0).getItem() instanceof EnchantedBookItem))
                this.sorti_catalyst.getStack(0).decrement(1);
            this.sorti_catalyzed[0] = 0;
            this.sorti_catalyzed[1] = 0;
            this.sorti_catalyzed[2] = 0;
        }
        return result;
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    public void moveCatalyst(PlayerEntity player, int slotid, CallbackInfoReturnable<ItemStack> cir) {
        if (EnchantingCatalyst.isDisabled())
            return;

        Slot slot = this.getSlot(slotid);
        ItemStack stack = this.inventory.getStack(0);

        if (slot.inventory == this.sorti_catalyst && !this.insertItem(slot.getStack(), 2, 38, true)) {
            slot.onTakeItem(player, slot.getStack());
            this.sorti_catalyst.markDirty();
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
            this.sorti_catalyzed[0] = 0;
            this.sorti_catalyzed[1] = 0;
            this.sorti_catalyzed[2] = 0;
        }
    }
}
