package net.lyof.sortilege;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.api.common.SolRegistries;
import net.lcc.sollib.api.common.logger.SolLogger;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.block.ModBlockEntities;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.ModItemGroups;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.loot.ModLootContexts;
import net.lyof.sortilege.recipe.loot.ModLootModifiers;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.setup.ModRuntime;
import net.lyof.sortilege.setup.ReloadListener;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public class Sortilege implements ModInitializer {
	public static final SolModContainer MOD = new SolModContainer("Sortilege", "sortilege");
	public static final String MOD_ID = MOD.getNamespace();

	@Override
	public void onInitialize() {
		MOD.createConfig("sortilege", 9, ModConfig::build);
		MOD.createConfig("sortilege-staffs", 9, ModConfig::buildStaffs);
		ModRuntime.load();

		ModBlocks.register();
		ModBlockEntities.register();

		ModAttributes.register();
		ModDataComponents.register();
		ModItems.register();
		ModItemGroups.register();

		ModEnchants.register();
		ModParticles.register();
		ModScreenHandlers.register();

		ModLootModifiers.register();
		ModLootContexts.register();
		ModRecipeTypes.register();

		registerPackets();
		registerModules();
		registerEvents();
	}

	private static void registerPackets() {
		PayloadTypeRegistry.playS2C().register(ModPackets.InitializePacket.TYPE, ModPackets.InitializePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CustomPotionData.TYPE, CustomPotionData.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ModPackets.InitializeLockPacket.TYPE, ModPackets.InitializeLockPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ModPackets.ParticlePacket.TYPE, ModPackets.ParticlePacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ModPackets.LapisShieldPacket.TYPE, ModPackets.LapisShieldPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(ModPackets.KnowledgeBook.TYPE, ModPackets.KnowledgeBook.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ModPackets.KnowledgeBook.TYPE, ModPackets.KnowledgeBook::run);
	}

	private static void registerModules() {
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(MOD.makeID("hd_particles"), container,
					Component.literal("HD Particles"), ResourcePackActivationType.NORMAL);
		});
	}

	private static void registerEvents() {
		SolRegistries.Data.RELOAD.register(ReloadListener.INSTANCE);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
			Sortilege.log().info("reloadevent", joined);
			List<CustomPacketPayload> packets = new ArrayList<>();

			packets.add(new ModPackets.InitializePacket());

			CustomPotionData.write(packets);
			RecipeLock.write(packets, player.getServer());

			packets.forEach(p -> ServerPlayNetworking.send(player, p));
		});
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			EnchantHelper.setRegistry(() -> server.registries().compositeAccess().registryOrThrow(Registries.ENCHANTMENT));
		});
	}

	public static SolLogger log() {
		return MOD.getLogger();
	}
}