package net.lyof.sortilege.mixin.sol;

import net.lcc.sollib.api.common.config.builder.IJsonBuilder;
import net.lcc.sollib.api.common.config.builder.JsonBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Stack;
import java.util.function.Consumer;

@Mixin(JsonBuilder.class)
public class JsonBuilderMixin {
    @Shadow private String currentPath;
    @Shadow @Final private Stack<String> path;

    @Inject(
            method = "addArray(Ljava/lang/String;Ljava/util/function/Consumer;)Lnet/lcc/sollib/api/common/config/builder/IJsonBuilder;",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER)
    )
    private void fixPath(String key, Consumer<IJsonBuilder.IArrayBuilder> consumer, CallbackInfoReturnable<IJsonBuilder> cir) {
        this.currentPath = String.join(".", this.path);
    }
}
