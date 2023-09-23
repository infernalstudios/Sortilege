package net.lyof.sortilege.events;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.items.armor.rendering.WitchHatModel;
import net.lyof.sortilege.items.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.particles.ModParticles;
import net.lyof.sortilege.particles.custom.WispParticle;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Sortilege.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WitchHatModel.LAYER_LOCATION, WitchHatModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerItemColor(RegisterColorHandlersEvent.Item event) {
        event.getItemColors().register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE.get());
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.register(ModParticles.WISP.get(), WispParticle::provider);
        event.register(ModParticles.WISP_PIXEL.get(), WispParticle::provider);
    }

    public static ShaderInstance PARTICLE_ADDITIVE_MULTIPLY;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        registerShader(event, "particle_add", DefaultVertexFormat.PARTICLE, (s) -> {
            PARTICLE_ADDITIVE_MULTIPLY = s;
        });
    }

    private static void registerShader(RegisterShadersEvent event, String id, VertexFormat format, Consumer<ShaderInstance> callback) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation(Sortilege.MOD_ID, id), format), callback);
    }
}
