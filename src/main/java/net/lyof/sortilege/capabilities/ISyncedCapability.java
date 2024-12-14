package net.lyof.sortilege.capabilities;

import net.minecraft.nbt.CompoundTag;

public interface ISyncedCapability {
    void syncDataToClient();

    CompoundTag saveForNetwork(CompoundTag tag);

    void loadFromNetwork(CompoundTag tag);
}
