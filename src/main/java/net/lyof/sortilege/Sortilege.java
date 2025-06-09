package net.lyof.sortilege;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import net.lyof.sortilege.recipe.enchanting.EnchantingCatalyst;
import net.lyof.sortilege.recipe.loot.ModLootModifiers;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.setup.ReloadListener;
import net.lyof.sortilege.setup.datagen.config.ConfiguredData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		ModLootModifiers.register();
		ModRecipeTypes.register();

		registerEvents();
	}

	private static void registerEvents() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ReloadListener.INSTANCE);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeInt(0);
			sender.sendPacket(ModPackets.INITIALIZE, packet);

			EnchantingCatalyst.write(sender);
			CustomPotionData.write(sender);
		});
	}


	public static Identifier makeID(String name) {
		return Identifier.of(MOD_ID, name);
	}

	@Deprecated
	public static <T> T log(T message) {
        LOGGER.info("[Sortilege] {}", message);
		return message;
	}
}