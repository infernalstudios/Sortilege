package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.core.component.DataComponentPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataComponentPredicate.class)
public class DataComponentPredicateMixin {
    @WrapOperation(method = "test(Lnet/minecraft/core/component/DataComponentMap;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
    private boolean testKnowledgeCompletion(Object a, Object b, Operation<Boolean> original) {
        boolean r = original.call(a, b);
        Sortilege.log().info(a, b);
        if (a instanceof EnchantKnowledge ak && b instanceof EnchantKnowledge bk) {
            Sortilege.log().info(ak.getAuthors(), bk.getCompletion());
            if (ak.getAuthors().contains("sortilege:isFull"))
                r |= bk.getCompletion() == 0;
        }
        return r;
    }
}
