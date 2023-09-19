package net.lyof.sortilege.particles.custom;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.lyof.sortilege.events.ClientEvents;
import net.lyof.sortilege.events.SetupEvents;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

public class WispRenderType implements ParticleRenderType {
    private ResourceLocation texture;
    public WispRenderType() {
    }

    public WispRenderType setTexture(ResourceLocation texture){
        this.texture = texture;
        return this;
    }

    @Override
    public void begin(BufferBuilder builder, TextureManager textureManager) {
        RenderSystem.enableBlend();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        GL11.glEnable(GL11.GL_BLEND);

        //
        RenderSystem.setShader(() -> ClientEvents.PARTICLE_ADDITIVE_MULTIPLY);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // opaque
        // RenderSystem.depthMask(true);
        // RenderSystem.disableBlend();
        // RenderSystem.enableLighting();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(Tesselator tessellator) {
        tessellator.end();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}