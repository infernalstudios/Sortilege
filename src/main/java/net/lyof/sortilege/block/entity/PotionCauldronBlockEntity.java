package net.lyof.sortilege.block.entity;

import net.lyof.sortilege.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.BlockPos;

public class PotionCauldronBlockEntity extends BlockEntity {
    public Potion potion = Potions.STRENGTH;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTION_CAULDRON, pos, state);
    }
}
