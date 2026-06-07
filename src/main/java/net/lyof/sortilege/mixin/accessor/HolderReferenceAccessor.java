package net.lyof.sortilege.mixin.accessor;

import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Holder.Reference.class)
public interface HolderReferenceAccessor<T> {
    @Accessor void setValue(T value);
}
