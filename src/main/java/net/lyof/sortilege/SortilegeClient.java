package net.lyof.sortilege;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.item.rendering.WitchHatRenderer;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.particle.custom.WispParticle;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreen;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.setup.ModRuntime;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;

public class SortilegeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRuntime.loadClient();

        ColorProviderRegistry.ITEM.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE);
        ColorProviderRegistry.BLOCK.register(PotionCauldronBlock::getBlockColor, ModBlocks.POTION_CAULDRON);

        ParticleFactoryRegistry.getInstance().register(ModParticles.WISP, WispParticle.Factory::new);

        MenuScreens.register(ModScreenHandlers.KNOWLEDGE_BOOK, KnowledgeBookScreen::new);

        if (ModConfig.witchHatEnabled.get()) ArmorRenderer.register(new WitchHatRenderer(), ModItems.WITCH_HAT);

        if (ModConfig.lapisShieldEnabled.get())
            ItemProperties.register(ModItems.LAPIS_SHIELD, Sortilege.MOD.makeID("cooldown"),
                    (stack, world, entity, seed) -> LapisShieldItem.isOnCooldown(stack) ? 1f : 0f);

        registerPackets();
    }

    private static void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.InitializePacket.TYPE, ModPackets.InitializePacket::run);
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.InitializeEnchantPacket.TYPE, ModPackets.InitializeEnchantPacket::run);
        ClientPlayNetworking.registerGlobalReceiver(CustomPotionData.TYPE, CustomPotionData::read);
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.InitializeLockPacket.TYPE, ModPackets.InitializeLockPacket::run);
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ParticlePacket.TYPE, ModPackets.ParticlePacket::run);
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.LapisShieldPacket.TYPE, ModPackets.LapisShieldPacket::run);
    }
}
