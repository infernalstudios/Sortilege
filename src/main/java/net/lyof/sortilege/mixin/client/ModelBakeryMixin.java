package net.lyof.sortilege.mixin.client;

import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Shadow protected abstract void loadSpecialItemModelAndDependencies(ModelResourceLocation modelLocation);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            ordinal = 1, shift = At.Shift.AFTER))
    public void loadPotionTextures(BlockColors blockColors, ProfilerFiller profilerFiller,
                                   Map<ResourceLocation, BlockModel> modelResources,
                                   Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResourcess, CallbackInfo ci) {

        if (!ModConfig.customPotionTextures.get()) return;

        for (ResourceLocation id : CustomPotionData.MODELS)
            this.loadSpecialItemModelAndDependencies(new ModelResourceLocation(id, "inventory"));

        for (String name : List.of("long_lingering", "long", "long_splash", "strong_lingering", "strong", "strong_splash"))
            this.loadSpecialItemModelAndDependencies(new ModelResourceLocation(Identifier.of("minecraft", name + "_potion"), "inventory"));
    }
}
