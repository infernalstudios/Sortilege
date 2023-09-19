package net.lyof.sortilege.particles.amo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.lyof.sortilege.Sortilege;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Sortilege.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleShaders {
    public static ShaderInstance PARTICLE_ADDITIVE_MULTIPLY;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        registerShader(event, "particle_add", DefaultVertexFormat.PARTICLE, (s) -> {
            PARTICLE_ADDITIVE_MULTIPLY = s;
        });
    }

    private static void registerShader(RegisterShadersEvent event, String id, VertexFormat format, Consumer<ShaderInstance> callback)
            throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation("sortilege", id), format), callback);
    }

    public static ParticleRenderType PARTICLE_SHEET_ADDITIVE_MULTIPLY = translucentSheet(() -> {
        return ParticleShaders.PARTICLE_ADDITIVE_MULTIPLY;
    });

    static ParticleRenderType translucentSheet(final Supplier<ShaderInstance> shader) {
        return new ParticleRenderType() {
            public void begin(BufferBuilder builder, TextureManager manager) {
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.setShader(shader);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            public void end(Tesselator tess) {
                tess.end();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }
        };
    }
}
