package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Once again I'm the master of stupid mixins
//   If you read this: I sincerely apologize, couldn't find a better way
//   Would appreciate a pull request if you can do better *without using AI*
@Mixin(targets = "net.minecraft.core.Registry$1")
public abstract class RegistryMixin<T> {
    @Shadow public abstract int size();
    @Shadow public abstract Holder<T> byId(int par1);

    @WrapMethod(method = "getId(Lnet/minecraft/core/Holder;)I")
    private int getLenientId(Holder<T> value, Operation<Integer> original) {
        int id = original.call(value);
        if (id != Registry.DEFAULT) return id;

        for (int i = 0; i < this.size(); i++) {
            Holder<T> v = this.byId(i);
            if (v != null && v.getRegisteredName().equals(value.getRegisteredName()))
                return i;
        }
        return Registry.DEFAULT;
    }
}
