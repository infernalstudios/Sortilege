package net.lyof.sortilege.setup;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModPackets {
    public static final Identifier INITIALIZE = Sortilege.makeID("initalize");
    public static final int INIT_RELOAD = 0;
    public static final int INIT_CATALYST = 1;
    public static final int INIT_POTION = 2;
    public static final int INIT_LOCK = 3;

    public static final Identifier WISP_PARTICLE_DISPLAY = Sortilege.makeID("wisp_particle_display");
    public static final Identifier LAPIS_SHIELD_COOLDOWN = Sortilege.makeID("lapis_shield_cooldown");

    public static final Identifier SET_KNOWLEDGE_AUTHORS = Sortilege.makeID("set_knowledge_authors");


    public static class Client {
        public static void initialize(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
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

        public static void wispParticleDisplay(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            int amount = buf.readInt(), spread = amount == 1 ? 0 : 2;

            client.execute(() -> {
                for (int i = 0; i < amount; i++) {
                    client.world.addImportantParticle(ModParticles.WISP_PIXEL, x + (0.5 - Math.random()) * spread,
                            y + (0.5 - Math.random()) * spread,
                            z + (0.5 - Math.random()) * spread,
                            r, g, b);
                }
            });
        }

        public static void lapisShieldCooldown(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            int id = buf.readInt();
            int cooldown = buf.readInt();

            client.execute(() -> {
                Entity e = handler.getWorld().getEntityById(id);
                if (!(e instanceof LivingEntity entity)) {
                    Sortilege.log("Something went wrong while receiving a packet", 2);
                    return;
                }
                ItemStack stack = entity.getOffHandStack();
                if (!ConfigEntries.lapisShieldEnabled || !stack.isOf(ModItems.LAPIS_SHIELD)) return;

                if (cooldown == 0)
                    LapisShieldItem.removeCooldown(stack);
                else
                    LapisShieldItem.addCooldown(stack, cooldown);
            });
        }
    }

    public static class Server {
        public static void setKnowledgeAuthors(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            List<String> authors = KnowledgeBookItem.getAuthors(buf.readItemStack());
            server.execute(() -> {
                if (handler.player.currentScreenHandler instanceof KnowledgeBookScreenHandler screen)
                    KnowledgeBookItem.setAuthors(screen.stack, authors);
            });
        }
    }
}
