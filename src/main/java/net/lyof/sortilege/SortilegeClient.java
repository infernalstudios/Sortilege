package net.lyof.sortilege;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.rendering.custom.WitchHatRenderer;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.particle.custom.WispParticle;
import net.lyof.sortilege.recipe.enchanting.EnchantingCatalyst;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.setup.ReloadListener;
import net.lyof.sortilege.setup.datagen.config.ConfiguredData;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class SortilegeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfiguredData.registerClient();
        registerPackets();

        ColorProviderRegistry.ITEM.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE);
        ColorProviderRegistry.BLOCK.register(PotionCauldronBlock::getBlockColor, ModBlocks.POTION_CAULDRON);

        ParticleFactoryRegistry.getInstance().register(ModParticles.WISP_PIXEL, WispParticle.Factory::new);

        if (ConfigEntries.witchHatEnabled) ArmorRenderer.register(new WitchHatRenderer(), ModItems.WITCH_HAT);

        if (ConfigEntries.lapisShieldEnabled)
            ModelPredicateProviderRegistry.register(ModItems.LAPIS_SHIELD, Sortilege.makeID("cooldown"), (stack, world, entity, seed) -> {
                stack.getOrCreateNbt();
                return LapisShieldItem.isOnCooldown(stack) ? 1f : 0f;
            });
    }

    private static void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.WISP_PARTICLE_DISPLAY, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
            float r = buf.readFloat(), g = buf.readFloat(), b = buf.readFloat();
            int amount = buf.readInt(), spread = amount == 1 ? 0 : 2;

            client.execute(() -> {
                for (int i = 0; i < amount; i++) {
                    client.world.addParticle(ModParticles.WISP_PIXEL, x + (0.5 - Math.random()) * spread,
                            y + (0.5 - Math.random()) * spread,
                            z + (0.5 - Math.random()) * spread,
                            r, g, b);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.LAPIS_SHIELD_COOLDOWN, (client, handler, buf, responseSender) -> {
            int id = buf.readInt();
            int cooldown = buf.readInt();

            client.execute(() -> {
                Entity e = handler.getWorld().getEntityById(id);
                if (!(e instanceof LivingEntity entity)) {
                    Sortilege.log("Something went wrong while receiving a packet");
                    return;
                }
                ItemStack stack = entity.getOffHandStack();
                if (!ConfigEntries.lapisShieldEnabled || !stack.isOf(ModItems.LAPIS_SHIELD)) return;

                if (cooldown == 0)
                    LapisShieldItem.removeCooldown(stack);
                else
                    LapisShieldItem.addCooldown(stack, cooldown);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.INITIALIZE, (client, handler, buf, responseSender) -> {
            int eventType = buf.readInt();

            if (eventType == 0)
                ReloadListener.INSTANCE.reloadClient();
            else if (eventType == 1)
                EnchantingCatalyst.read(buf);
            else if (eventType == 2)
                CustomPotionData.read(buf);
        });
    }
}
