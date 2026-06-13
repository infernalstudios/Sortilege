package net.lyof.sortilege.block;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static void register() {}

    public static <T extends BlockEntityType<?>> T register(String name, T blockEntityType) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Sortilege.MOD.makeID(name), blockEntityType);
    }

    public static final BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON = register(
            "potion_cauldron",
            BlockEntityType.Builder.of(PotionCauldronBlockEntity::new, ModBlocks.POTION_CAULDRON).build(null)
    );
}
