package net.lyof.sortilege.setup;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ModPackets {
    public static final ResourceLocation INITIALIZE = Sortilege.MOD.makeID("initalize");
    public static final int INIT_RELOAD = 0;
    public static final int INIT_CATALYST = 1;
    public static final int INIT_POTION = 2;
    public static final int INIT_LOCK = 3;

    public static final ResourceLocation WISP_PARTICLE_DISPLAY = Sortilege.MOD.makeID("wisp_particle_display");
    public static final ResourceLocation LAPIS_SHIELD_COOLDOWN = Sortilege.MOD.makeID("lapis_shield_cooldown");

    public static final ResourceLocation SET_KNOWLEDGE_AUTHORS = Sortilege.MOD.makeID("set_knowledge_authors");


    public static class Client {
        public static void initialize(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            int eventType = buf.readInt();

            if (eventType == INIT_RELOAD)
                ReloadListener.INSTANCE.reloadClient();
            else if (eventType == INIT_CATALYST)
                EnchantingCatalyst.read(buf);
            else if (eventType == INIT_POTION)
                CustomPotionData.read(buf);
            else if (eventType == INIT_LOCK)
                RecipeLock.read(buf);
        }

        public static void wispParticleDisplay(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            int amount = buf.readInt(), spread = amount == 1 ? 0 : 2;

            client.execute(() -> {
                for (int i = 0; i < amount; i++) {
                    client.level.addAlwaysVisibleParticle(ModParticles.WISP_PIXEL, x + (0.5 - Math.random()) * spread,
                            y + (0.5 - Math.random()) * spread,
                            z + (0.5 - Math.random()) * spread,
                            r, g, b);
                }
            });
        }

        public static void lapisShieldCooldown(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            int id = buf.readInt();
            int cooldown = buf.readInt();

            client.execute(() -> {
                Entity e = handler.getLevel().getEntity(id);
                if (!(e instanceof LivingEntity entity)) {
                    //Sortilege.log("Something went wrong while receiving a packet", 2);
                    return;
                }
                ItemStack stack = entity.getOffhandItem();
                if (!ModConfig.lapisShieldEnabled.get() || !stack.is(ModItems.LAPIS_SHIELD)) return;

                if (cooldown == 0)
                    LapisShieldItem.removeCooldown(stack);
                else
                    LapisShieldItem.addCooldown(stack, cooldown);
            });
        }
    }

    public static class Server {
        public static void setKnowledgeAuthors(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
            List<String> authors = KnowledgeBookItem.getAuthors(buf.readItem());
            server.execute(() -> {
                if (handler.player.containerMenu instanceof KnowledgeBookScreenHandler screen)
                    KnowledgeBookItem.setAuthors(screen.stack, authors);
            });
        }
    }
}
