package net.lyof.sortilege.block;

import net.lcc.sollib.api.common.registry.SHolder;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static class BlockEntityHolder<T extends BlockEntity> extends SHolder<BlockEntityType<T>> {
        public BlockEntityHolder(SolModContainer mod, String name, Supplier<BlockEntityType<T>> entrySupplier) {
            super(mod, name, entrySupplier);
        }

        @Override
        public Registry<BlockEntityType<T>> getRegistry() {
            return (Registry<BlockEntityType<T>>) (Object) BuiltInRegistries.BLOCK_ENTITY_TYPE;
        }
    }

    public static void register() {}

    public static <T extends BlockEntity, E extends BlockEntityType<T>> BlockEntityHolder<T> register(String name, Supplier<E> blockEntityType) {
        return Sortilege.MOD.register(BlockEntityHolder.class, name, blockEntityType);
    }

    public static final Supplier<BlockEntityType<PotionCauldronBlockEntity>> POTION_CAULDRON = register("potion_cauldron",
            () -> BlockEntityType.Builder.of(PotionCauldronBlockEntity::new, ModBlocks.POTION_CAULDRON.get()).build(null)
    );
}
