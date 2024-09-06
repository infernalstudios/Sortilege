package net.lyof.sortilege.particle.amo;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;

public class ParticleShaders {
    public static ShaderProgram PARTICLE_ADDITIVE_MULTIPLY;

    public static void register(ShaderProgram s) {
        PARTICLE_ADDITIVE_MULTIPLY = s;
    }

    public static ParticleTextureSheet PARTICLE_SHEET_ADDITIVE_MULTIPLY = new ParticleTextureSheet() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(() -> PARTICLE_ADDITIVE_MULTIPLY);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        @Override
        public void draw(Tessellator tessellator) {
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    };
}
