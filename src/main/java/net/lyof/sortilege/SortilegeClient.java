package net.lyof.sortilege;

import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.particle.custom.WispParticle;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreen;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModRuntime;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = Sortilege.MOD_ID, dist = Dist.CLIENT)
public class SortilegeClient {
    public SortilegeClient(FMLModContainer container, IEventBus modBus, Dist dist) {
        ModRuntime.loadClient();
        EnchantHelper.setRegistry(() -> Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT));

        /*TODO
        if (ModConfig.witchHatEnabled.get()) ArmorRenderer.register(new WitchHatRenderer(), ModItems.WITCH_HAT);*/
/*
        if (ModConfig.lapisShieldEnabled.get())
            ItemProperties.register(ModItems.LAPIS_SHIELD.get(), Sortilege.MOD.makeID("cooldown"),
                    (stack, world, entity, seed) -> LapisShieldItem.isOnCooldown(stack) ? 1f : 0f);*/
    }

    @SubscribeEvent
    public static void onEvent(RegisterColorHandlersEvent.Item event) {
        event.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE.get());
    }

    @SubscribeEvent
    public static void onEvent(RegisterColorHandlersEvent.Block event) {
        event.register(PotionCauldronBlock::getBlockColor, ModBlocks.POTION_CAULDRON.get());
    }

    @SubscribeEvent
    public static void onEvent(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.WISP.get(), WispParticle.Factory::new);
    }

    @SubscribeEvent
    public static void onEvent(RegisterMenuScreensEvent event) {
        event.register(ModScreenHandlers.KNOWLEDGE_BOOK.get(), KnowledgeBookScreen::new);
    }
}
