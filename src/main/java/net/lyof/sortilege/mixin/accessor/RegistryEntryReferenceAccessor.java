package net.lyof.sortilege.mixin.accessor;

import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryEntry.Reference.class)
public interface RegistryEntryReferenceAccessor<T> {
    @Accessor
    void setValue(T value);
}
