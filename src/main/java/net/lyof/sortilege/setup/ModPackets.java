package net.lyof.sortilege.setup;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ModPackets {
    public static final Identifier INITIALIZE = Sortilege.makeID("initalize");

    public static final Identifier WISP_PARTICLE_DISPLAY = Sortilege.makeID("wisp_particle_display");
    public static final Identifier LAPIS_SHIELD_COOLDOWN = Sortilege.makeID("lapis_shield_cooldown");


    public static class Client {
        public static void initialize(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            int eventType = buf.readInt();

            if (eventType == 0)
                ReloadListener.INSTANCE.reloadClient();
            else if (eventType == 1)
                EnchantingCatalyst.read(buf);
            else if (eventType == 2)
                CustomPotionData.read(buf);
            else if (eventType == 3)
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
}
