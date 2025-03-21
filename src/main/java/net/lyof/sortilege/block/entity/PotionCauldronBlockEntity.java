package net.lyof.sortilege.block.entity;

import net.lyof.sortilege.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PotionCauldronBlockEntity extends BlockEntity {
    public Potion potion = Potions.EMPTY;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTION_CAULDRON, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("potion", Registries.POTION.getId(this.potion).toString());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.potion = Registries.POTION.get(new Identifier(nbt.getString("potion")));
    }
}
