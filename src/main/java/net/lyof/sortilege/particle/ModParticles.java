package net.lyof.sortilege.particle;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class ModParticles {
    public static void sendParticles(Level world, double x, double y, double z, int amount, int color) {
        sendParticles(world, ModParticles.WISP_ID, x, y, z, amount, color);
    }

    public static void sendParticles(Level world, ResourceLocation particle, double x, double y, double z, int amount, int color) {
        if (!world.isClientSide()) {
            ModPackets.ParticlePacket packet = new ModPackets.ParticlePacket(particle, new Vector3f((float) x, (float) y, (float) z), color, amount);

            for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) world, new BlockPos((int) x, (int) y, (int) z)))
                ServerPlayNetworking.send(player, packet);
        }
    }


    public static void register() {}

    public static final ResourceLocation WISP_ID = Sortilege.MOD.makeID("wisp");
    public static final ParticleType<ColorParticleOption> WISP = Registry.register(BuiltInRegistries.PARTICLE_TYPE, WISP_ID,
            new ParticleType<ColorParticleOption>(false) {
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });
}
