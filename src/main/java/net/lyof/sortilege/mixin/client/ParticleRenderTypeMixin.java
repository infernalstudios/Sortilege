package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.particle.ParticleRenderType$4")
public abstract class ParticleRenderTypeMixin {
    @Shadow public abstract String toString();

    // I am aware this is dirty work, and I'm sorry about it, I could not find a cleaner way
    @WrapMethod(method = "begin")
    public void translucentLitBegin(BufferBuilder builder, TextureManager textureManager, Operation<Void> original) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @WrapMethod(method = "end")
    public void translucentLitDraw(Tesselator tessellator, Operation<Void> original) {
        tessellator.end();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
