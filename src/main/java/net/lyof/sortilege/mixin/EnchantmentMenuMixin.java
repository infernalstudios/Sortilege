package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantLearner;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.inject.EnchantInfoHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
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
import java.util.stream.Stream;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu implements EnchantInfoHolder {
    @Shadow @Final private Container enchantSlots;
    @Shadow @Final private ContainerLevelAccess access;
    @Shadow @Final private RandomSource random;
    @Shadow @Final public int[] costs;

    protected EnchantmentMenuMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Unique private final Container sorti_catalyst = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentMenuMixin.this.slotsChanged(EnchantmentMenuMixin.this.enchantSlots);
        }
    };
    @Unique private final int[] sorti_catalyzed = new int[3];
    @Unique private EnchantKnowledge sorti_knowledge = null;
    @Unique private Player sorti_player = null;

    @Override
    public boolean sorti_isCatalyzed(int slot) {
        return this.sorti_catalyzed[slot] == 1;
    }

    @Override
    public boolean sorti_hasCatalyst() {
        return !this.sorti_catalyst.getItem(0).isEmpty();
    }

    @Override
    public boolean sorti_hasEnchantableItem() {
        ItemStack stack = this.enchantSlots.getItem(0);
        return !stack.isEmpty() && stack.isEnchantable() && !EnchantingCatalyst.isDisabled();
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At(value = "TAIL"))
    public void setupLogics(int syncId, Inventory inventory, ContainerLevelAccess context, CallbackInfo ci) {
        this.sorti_player = inventory.player;

        if (!EnchantingCatalyst.isDisabled()) {
            this.addSlot(new Slot(this.sorti_catalyst, 0, 25, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return EnchantingCatalyst.isCatalyst(stack);
                }

                @Override
                public boolean isActive() {
                    ItemStack stack = EnchantmentMenuMixin.this.enchantSlots.getItem(0);
                    return !stack.isEmpty() && stack.isEnchantable();
                }
            });

            this.addDataSlot(DataSlot.shared(this.sorti_catalyzed, 0));
            this.addDataSlot(DataSlot.shared(this.sorti_catalyzed, 1));
            this.addDataSlot(DataSlot.shared(this.sorti_catalyzed, 2));
        }
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    public void updateLogics(Container inventory, CallbackInfo ci) {
        if (ModConfig.knowledgeEnabled.get()) {
            this.sorti_knowledge = ((EnchantLearner) this.sorti_player).sorti_getKnowledge(null);

            this.access.execute((world, pos) -> {
                for (BlockPos p : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                    if (!(world.getBlockEntity(pos.offset(p)) instanceof ChiseledBookShelfBlockEntity bookshelf)) continue;

                    ItemStack stack;
                    for (int i = 0; i < ChiseledBookShelfBlockEntity.MAX_BOOKS_IN_STORAGE; i++) {
                        stack = bookshelf.getItem(i);
                        if (stack.is(ModItems.KNOWLEDGE_BOOK) && stack.get(ModDataComponents.KNOWLEDGE).isAuthor(sorti_player))
                            this.sorti_knowledge.learn(stack);
                    }
                }
            });
        }

        ItemStack stack = this.enchantSlots.getItem(0);
        if (stack.isEmpty() || !stack.isEnchantable()) {
            this.sorti_catalyzed[0] = 0;
            this.sorti_catalyzed[1] = 0;
            this.sorti_catalyzed[2] = 0;
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    public void dropCatalyst(Player player, CallbackInfo ci) {
        this.access.execute((world, pos) -> this.clearContainer(player, this.sorti_catalyst));
    }

    @WrapOperation(method = "getEnchantmentList", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;selectEnchantment(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;"))
    public List<EnchantmentInstance> overrideDefaultEnchanting(RandomSource random, ItemStack stack, int level,
                                                               Stream<Holder<Enchantment>> possibleEnchantments,
                                                               Operation<List<EnchantmentInstance>> original) {
        if (ModConfig.catalystOnly.get())
            return new ArrayList<>();
        return original.call(random, stack, level, possibleEnchantments);
    }

    @ModifyReturnValue(method = "getEnchantmentList", at = @At("RETURN"))
    public List<EnchantmentInstance> generateSortiEnchantments(List<EnchantmentInstance> original, RegistryAccess registryAccess,
                                                               ItemStack stack, int slot, int cost) {
        int power = this.costs[slot];
        this.sorti_catalyzed[slot] = 0;

        // We need a mutable list
        List<EnchantmentInstance> result = new ArrayList<>(original);

        // Knowledge logic
        if (ModConfig.knowledgeEnabled.get()) {
            result.removeIf(entry -> !this.sorti_knowledge.isKnown(entry.enchantment));
            List<EnchantmentInstance> list = new ArrayList<>();
            for (EnchantmentInstance entry : result)
                list.add(new EnchantmentInstance(entry.enchantment,
                        Math.min(entry.level, this.sorti_knowledge.getKnownLevel(entry.enchantment))));
            result = list;
        }

        // Catalyst logic
        ItemEnchantments enchants = EnchantingCatalyst.getEnchantments(this.sorti_catalyst.getItem(0));
        if (!enchants.isEmpty()) {
            // Randomize seed
            for (int i = 0; i < slot; i++) this.random.nextDouble();

            // Get catalyzed enchant
            Holder<Enchantment> chosen = MathHelper.randi(enchants.keySet().stream().filter(enchant ->
                    enchant.value().canEnchant(stack) || stack.is(Items.BOOK)).toList(), this.random);
            if (chosen == null) return original;

            // Randomize seed
            for (int i = 0; i < registryAccess.registryOrThrow(Registries.ENCHANTMENT).getId(chosen.value()) % 8; i++)
                this.random.nextDouble();

            // Catalysts don't always apply
            if (this.random.nextDouble() <= ModConfig.catalystChance.get()) {
                int lvl = chosen.value().getMaxLevel();
                // Higher level in costier slots
                for (int i = 0; i < 3 - slot; i++)
                    lvl = Math.min(lvl, this.random.nextInt(chosen.value().getMaxLevel()));
                lvl += 1;

                // Nerf books, because they are reusable
                if (this.sorti_catalyst.getItem(0).getItem() instanceof EnchantedBookItem)
                    lvl = Math.min(lvl, enchants.getLevel(chosen));

                // Remove incompatible and duplicate enchants
                result.removeIf(entry -> !Enchantment.areCompatible(chosen, entry.enchantment));
                result.add(0, new EnchantmentInstance(chosen, lvl));

                // Slot was successfully catalyzed
                this.sorti_catalyzed[slot] = 1;
                this.costs[slot] = power;
            }
        }

        if (result.isEmpty()) this.costs[slot] = 0;
        return result;
    }

    @WrapMethod(method = "clickMenuButton")
    public boolean useCatalyst(Player player, int id, Operation<Boolean> original) {
        boolean result = original.call(player, id);

        if (EnchantingCatalyst.isDisabled())
            return result;

        if (result) {
            if (!(this.sorti_catalyst.getItem(0).getItem() instanceof EnchantedBookItem))
                this.sorti_catalyst.getItem(0).shrink(1);
            this.sorti_catalyzed[0] = 0;
            this.sorti_catalyzed[1] = 0;
            this.sorti_catalyzed[2] = 0;
        }
        return result;
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    public void moveCatalyst(Player player, int slotid, CallbackInfoReturnable<ItemStack> cir) {
        if (EnchantingCatalyst.isDisabled())
            return;

        Slot slot = this.getSlot(slotid);
        ItemStack stack = this.enchantSlots.getItem(0);

        if (slot.container == this.sorti_catalyst && !this.moveItemStackTo(slot.getItem(), 2, 38, true)) {
            slot.onTake(player, slot.getItem());
            this.sorti_catalyst.setChanged();
            cir.setReturnValue(ItemStack.EMPTY);
        }
        else if (EnchantingCatalyst.isCatalyst(slot.getItem())
                && !stack.isEmpty() && stack.isEnchantable() && !this.moveItemStackTo(slot.getItem(), 38, 39, true)) {

            slot.onTake(player, slot.getItem());
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
