package net.lyof.sortilege;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.armor.rendering.WitchHatRenderer;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.particle.amo.ParticleShaders;
import net.lyof.sortilege.particle.custom.WispParticle;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class SortilegeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE);
        ColorProviderRegistry.BLOCK.register(PotionCauldronBlock::getBlockColor, ModBlocks.POTION_CAULDRON);

        ParticleFactoryRegistry.getInstance().register(ModParticles.WISP_PIXEL, WispParticle.Factory::new);
        /*CoreShaderRegistrationCallback.EVENT.register(shader ->
                shader.register(Sortilege.makeID("particle"), VertexFormats.POSITION_TEXTURE_COLOR_LIGHT,
                        ParticleShaders::register)
        );*/
        if (ConfigEntries.witchHatEnabled) ArmorRenderer.register(new WitchHatRenderer(), ModItems.WITCH_HAT);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.WISP_PARTICLE_DISPLAY, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            int amount = buf.readInt(), spread = amount == 1 ? 0 : 1;
            client.execute(() -> {
                for (int i = 0; i < amount; i++) {
                    client.world.addParticle(ModParticles.WISP_PIXEL, x + (0.5 - Math.random()) * spread,
                            y + (0.5 - Math.random()) * spread,
                            z + (0.5 - Math.random()) * spread,
                            r, g, b);
                }
            });
        });
    }
}
