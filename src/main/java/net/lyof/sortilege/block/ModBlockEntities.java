package net.lyof.sortilege.block;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static void register() {}

    public static <T extends BlockEntityType<?>> T register(String name, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Sortilege.makeID(name), blockEntityType);
    }

    public static final BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON = register(
            "potion_cauldron",
            BlockEntityType.Builder.create(PotionCauldronBlockEntity::new, ModBlocks.POTION_CAULDRON).build(null)
    );
}
