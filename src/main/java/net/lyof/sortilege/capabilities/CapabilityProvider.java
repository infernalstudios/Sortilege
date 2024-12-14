package net.lyof.sortilege.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class CapabilityProvider<C extends IPersistentCapability<C>> implements ICapabilitySerializable<CompoundTag> {
    private static final NonNullSupplier<IllegalStateException> EXCEPTION =
        () -> new IllegalStateException("Capability handler was holding a null or empty capability!");

    private final LazyOptional<C> capabilityHandler;
    private final Lazy<Capability<C>> instance;

    public CapabilityProvider(C capability) {
        this.capabilityHandler = LazyOptional.of(() -> capability);
        this.instance = Lazy.of(capability::getDefaultInstance);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == this.instance.get() ? this.capabilityHandler.cast() : LazyOptional.empty();
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        this.get().load(compound);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.get().save(new CompoundTag());
    }

    private C get() {
        return this.capabilityHandler.orElseThrow(EXCEPTION);
    }
}
