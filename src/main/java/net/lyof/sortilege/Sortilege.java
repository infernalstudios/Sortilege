package net.lyof.sortilege;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.api.common.config.SolConfig;
import net.lcc.sollib.api.common.logger.SolLogger;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.block.ModBlockEntities;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModItemGroups;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.recipe.loot.ModLootModifiers;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.setup.ReloadListener;
import net.lyof.sortilege.setup.datagen.config.ConfiguredData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import java.util.ArrayList;
import java.util.List;

public class Sortilege implements ModInitializer {
	public static final SolModContainer MOD = new SolModContainer("Sortilege", "sortilege");
	public static final String MOD_ID = MOD.getNamespace();

	public static final SolConfig CONFIG = MOD.createConfig("sortilege", 3, ModConfig::build);

	@Override
	public void onInitialize() {
		ConfiguredData.register();

		ModBlocks.register();
		ModBlockEntities.register();

		ModAttributes.register();
		ModItems.register();
		ModItemGroups.register();

		ModEnchants.register();
		ModParticles.register();
		ModScreenHandlers.register();

		ModLootModifiers.register();
		ModRecipeTypes.register();

		registerPackets();
		registerModules();
		registerEvents();
	}

	private static void registerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(ModPackets.SET_KNOWLEDGE_AUTHORS, ModPackets.Server::setKnowledgeAuthors);
	}

	private static void registerModules() {
		if (FabricLoader.getInstance().isModLoaded("miningmaster") && ModConfig.miningMasterIntegration.get())
			registerPack("compat_miningmaster", "Mining Master Compat", false);
	}

	private static void registerPack(String id, String name, boolean force) {
		Sortilege.log().info("Enabling module : " + name);
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container ->
				ResourceManagerHelper.registerBuiltinResourcePack(MOD.makeID(id), container, Component.literal(name),
						force ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.DEFAULT_ENABLED));
	}

	private static void registerEvents() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ReloadListener.INSTANCE);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
			List<FriendlyByteBuf> packets = new ArrayList<>();

			FriendlyByteBuf packet = PacketByteBufs.create();
			packet.writeInt(0);
			packets.add(packet);

			EnchantingCatalyst.write(packets);
			CustomPotionData.write(packets);
			RecipeLock.write(packets, player);

			packets.forEach(p -> ServerPlayNetworking.send(player, ModPackets.INITIALIZE, p));
		});
	}

	public static SolLogger log() {
		return MOD.getLogger();
	}
}