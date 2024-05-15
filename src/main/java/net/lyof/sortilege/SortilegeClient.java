package net.lyof.sortilege;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.particles.ModParticles;
import net.lyof.sortilege.particles.amo.ParticleShaders;
import net.lyof.sortilege.particles.custom.WispParticle;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.SpriteAtlasManager;

public class SortilegeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE);

        ParticleFactoryRegistry.getInstance().register(ModParticles.WISP_PIXEL, WispParticle.Factory::new);
        CoreShaderRegistrationCallback.EVENT.register(shader -> {
            shader.register(Sortilege.makeID("particle"), VertexFormats.POSITION_TEXTURE_COLOR_LIGHT,
                    ParticleShaders::register);
        });
    }
}
