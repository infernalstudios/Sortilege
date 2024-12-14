package net.lyof.sortilege.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;

public interface IPersistentCapability<C> {
    Capability<C> getDefaultInstance();

    CompoundTag save(CompoundTag tag);

    void load(CompoundTag tag);
}
