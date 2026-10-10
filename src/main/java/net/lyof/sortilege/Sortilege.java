package net.lyof.sortilege;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

@Mod(Sortilege.MOD_ID)
public class Sortilege {
	public static final String MOD_ID = "sortilege";
	public static final SolModContainer MOD = new SolModContainer("Sortilege", MOD_ID);

	public Sortilege(IEventBus eventBus, ModContainer container) {
		MOD.createConfig("sortilege", 9, ModConfig::build);
		MOD.createConfig("sortilege-staffs", 9, ModConfig::buildStaffs);
		ModRuntime.load();

		ModBlocks.register();
		ModBlockEntities.register();

		ModAttributes.register();
		ModDataComponents.register();
		ModItems.register();
		eventBus.addListener(ModItemGroups::register);

		ModEnchants.register();
		ModParticles.register();
		ModScreenHandlers.register();

		ModLootContexts.register();
		ModRecipeTypes.register();

		SolRegistries.Data.RELOAD.register(ReloadListener.INSTANCE);
	}

	@SubscribeEvent
	private static void onEvent(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");

		registrar.playToClient(ModPackets.InitializePacket.TYPE, ModPackets.InitializePacket.STREAM_CODEC, ModPackets.InitializePacket::run);
		registrar.playToClient(CustomPotionData.TYPE, CustomPotionData.STREAM_CODEC, CustomPotionData::read);
		registrar.playToClient(ModPackets.InitializeLockPacket.TYPE, ModPackets.InitializeLockPacket.STREAM_CODEC, ModPackets.InitializeLockPacket::run);
		registrar.playToClient(ModPackets.ParticlePacket.TYPE, ModPackets.ParticlePacket.STREAM_CODEC, ModPackets.ParticlePacket::run);
		registrar.playToClient(ModPackets.LapisShieldPacket.TYPE, ModPackets.LapisShieldPacket.STREAM_CODEC, ModPackets.LapisShieldPacket::run);

		registrar.playToServer(ModPackets.KnowledgeBook.TYPE, ModPackets.KnowledgeBook.STREAM_CODEC, ModPackets.KnowledgeBook::run);
	}

	@SubscribeEvent
	public static void onEvent(AddPackFindersEvent event) {
		event.addPackFinders(MOD.makeID("hd_particles"), PackType.CLIENT_RESOURCES,
				Component.literal("HD Particles"), PackSource.BUILT_IN, false, Pack.Position.TOP);
	}

	@SubscribeEvent
	public static void onEvent(OnDatapackSyncEvent event) {
		List<CustomPacketPayload> packets = new ArrayList<>();
		packets.add(new ModPackets.InitializePacket(true));

		CustomPotionData.write(packets);
		RecipeLock.write(packets, event.getPlayer().getServer());

		packets.add(new ModPackets.InitializePacket(false));
		packets.forEach(p -> PacketDistributor.sendToPlayer(event.getPlayer(), p));
	}

	@SubscribeEvent
	public static void onEvent(ServerStartingEvent event) {
		EnchantHelper.setRegistry(() -> event.getServer().registries().compositeAccess().registryOrThrow(Registries.ENCHANTMENT));
	}


	public static SolLogger log() {
		return MOD.getLogger();
	}
}