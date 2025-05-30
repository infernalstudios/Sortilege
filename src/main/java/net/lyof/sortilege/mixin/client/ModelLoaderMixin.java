package net.lyof.sortilege.mixin.client;

import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow protected abstract void addModel(ModelIdentifier modelId);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 3, shift = At.Shift.AFTER))
    public void loadPotionTextures(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels,
                                   Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {

        for (Identifier id : CustomPotionData.MODELS)
            this.addModel(new ModelIdentifier(id, "inventory"));

        for (String name : List.of("long_lingering", "long", "long_splash", "strong_lingering", "strong", "strong_splash"))
            this.addModel(new ModelIdentifier(Identifier.of("minecraft", name + "_potion"), "inventory"));
    }
}
