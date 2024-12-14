package net.lyof.sortilege.capabilities;

import net.minecraft.nbt.CompoundTag;

public interface ISyncedPersistentCapability<C> extends IPersistentCapability<C>, ISyncedCapability {
    @Override
    default CompoundTag saveForNetwork(CompoundTag tag) {
        return this.save(tag);
    }

    @Override
    default void loadFromNetwork(CompoundTag tag) {
        this.load(tag);
    }
}
