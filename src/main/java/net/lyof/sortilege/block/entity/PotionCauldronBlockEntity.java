package net.lyof.sortilege.block.entity;

import net.lyof.sortilege.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PotionCauldronBlockEntity extends BlockEntity {
    public Potion potion = Potions.EMPTY;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTION_CAULDRON, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.potion = BuiltInRegistries.POTION.get(new ResourceLocation(nbt.getString("potion")));
    }

    @Override
    public @Nullable Object getRenderData() {
        return PotionUtils.getColor(this.potion);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        this.saveAdditional(nbt);
        return nbt;
    }
}
