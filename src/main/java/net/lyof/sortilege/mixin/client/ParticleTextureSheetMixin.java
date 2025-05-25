package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.particle.ParticleTextureSheet$4")
public abstract class ParticleTextureSheetMixin {
    @Shadow public abstract String toString();

    // I am aware this is dirty work, and I'm sorry about it, I could not find a cleaner way
    @WrapMethod(method = "begin")
    public void translucentLitBegin(BufferBuilder builder, TextureManager textureManager, Operation<Void> original) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
    }

    @WrapMethod(method = "draw")
    public void translucentLitDraw(Tessellator tessellator, Operation<Void> original) {
        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
