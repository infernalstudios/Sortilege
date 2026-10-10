package net.lyof.sortilege.particle;

import com.mojang.serialization.MapCodec;
import net.lcc.sollib.api.common.registry.SHolder;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ModParticles {
    public static void sendParticles(Level world, double x, double y, double z, int amount, int color) {
        sendParticles(world, ModParticles.WISP_ID, x, y, z, amount, color);
    }

    public static void sendParticles(Level world, ResourceLocation particle, double x, double y, double z, int amount, int color) {
        if (!world.isClientSide()) {
            ModPackets.ParticlePacket packet = new ModPackets.ParticlePacket(particle, new Vector3f((float) x, (float) y, (float) z), color, amount);

            PacketDistributor.sendToPlayersNear((ServerLevel) world, null, x, y, z, 256, packet);
        }
    }


    public static class ParticleHolder<T extends ParticleOptions> extends SHolder<ParticleType<T>> {
        public ParticleHolder(SolModContainer mod, String name, Supplier<ParticleType<T>> entrySupplier) {
            super(mod, name, entrySupplier);
        }

        @Override
        public Registry<ParticleType<T>> getRegistry() {
            return (Registry<ParticleType<T>>) (Object) BuiltInRegistries.PARTICLE_TYPE;
        }
    }


    public static void register() {}

    public static final ResourceLocation WISP_ID = Sortilege.MOD.makeID("wisp");
    public static final ParticleHolder<ColorParticleOption> WISP = Sortilege.MOD.register(
            ParticleHolder.class,
            WISP_ID.getNamespace(),
            () -> new ParticleType<ColorParticleOption>(false) {
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });
}
