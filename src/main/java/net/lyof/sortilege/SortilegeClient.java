package net.lyof.sortilege;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.rendering.custom.WitchHatRenderer;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.particle.custom.WispParticle;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.setup.datagen.config.ConfiguredData;
import net.minecraft.client.item.ModelPredicateProviderRegistry;

public class SortilegeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfiguredData.registerClient();
        registerPackets();

        ColorProviderRegistry.ITEM.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE);
        ColorProviderRegistry.BLOCK.register(PotionCauldronBlock::getBlockColor, ModBlocks.POTION_CAULDRON);

        ParticleFactoryRegistry.getInstance().register(ModParticles.WISP_PIXEL, WispParticle.Factory::new);

        if (ConfigEntries.witchHatEnabled) ArmorRenderer.register(new WitchHatRenderer(), ModItems.WITCH_HAT);

        if (ConfigEntries.lapisShieldEnabled)
            ModelPredicateProviderRegistry.register(ModItems.LAPIS_SHIELD, Sortilege.makeID("cooldown"), (stack, world, entity, seed) -> {
                stack.getOrCreateNbt();
                return LapisShieldItem.isOnCooldown(stack) ? 1f : 0f;
            });
    }

    private static void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.INITIALIZE, ModPackets.Client::initialize);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.WISP_PARTICLE_DISPLAY, ModPackets.Client::wispParticleDisplay);
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.LAPIS_SHIELD_COOLDOWN, ModPackets.Client::lapisShieldCooldown);
    }
}
