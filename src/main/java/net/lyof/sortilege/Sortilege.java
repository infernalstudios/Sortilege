package net.lyof.sortilege;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.block.ModBlockEntities;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.config.ModConfig;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Sortilege implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Sortilege");
	public static final String MOD_ID = "sortilege";

	@Override
	public void onInitialize() {
		ModConfig.register();
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

		registerEvents();
		registerPackets();
	}

	private static void registerEvents() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ReloadListener.INSTANCE);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
			List<PacketByteBuf> packets = new ArrayList<>();

			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeInt(0);
			packets.add(packet);

			EnchantingCatalyst.write(packets);
			CustomPotionData.write(packets);
			RecipeLock.write(packets, player);

			packets.forEach(p -> ServerPlayNetworking.send(player, ModPackets.INITIALIZE, p));
		});
	}

	private static void registerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(ModPackets.SET_KNOWLEDGE_AUTHORS, ModPackets.Server::setKnowledgeAuthors);
	}


	public static Identifier makeID(String name) {
		return Identifier.of(MOD_ID, name);
	}

	@Deprecated
	public static <T> T log(T message) {
		return log(message, 0);
	}

	public static <T> T log(T message, int level) {
		if (level == 0)
        	LOGGER.info("[Sortilege] {}", message);
		else if (level == 1)
			LOGGER.warn("[Sortilege] {}", message);
		else if (level == 2)
			LOGGER.error("[Sortilege] {}", message);
		else if (level == 3)
			LOGGER.debug("[Sortilege] {}", message);
		return message;
	}
}