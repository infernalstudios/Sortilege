package net.lyof.sortilege.particles;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.particles.custom.WispParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.antlr.v4.runtime.misc.Triple;

public class ModParticles {
    @Deprecated
    public static void spawnWisps(ServerLevel world, double x, double y, double z, int amount, Triple<Float, Float, Float> color) {
        //ParticleOptions particle = ConfigEntries.staffsHdParticles ? WISP.get() : WISP_PIXEL.get();
        double spread = amount > 1 ? 0.4 : 0;
        WispParticle.COLOR = color;
        world.sendParticles(ParticleTypes.INSTANT_EFFECT, x, y, z, amount, spread, spread, spread, 0);
    }

    public static void spawnWisps(Level level, double x, double y, double z, int amount, Triple<Float, Float, Float> color) {
        if (level.isClientSide()) {
            WispParticle.COLOR = color;
            double spread = amount == 1 ? 0: 1;
            for (int i = 0; i < amount; i++)
                level.addParticle(WISP_PIXEL.get(), x, y, z, (Math.random() - 0.5) * spread, spread, (Math.random() - 0.5) * spread);
        }
    }


    public static final DeferredRegister<ParticleType<?>> PARTICLES_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Sortilege.MOD_ID);

    public static void register(IEventBus eventbus) {
        PARTICLES_TYPES.register(eventbus);
    }


    public static final RegistryObject<SimpleParticleType> WISP_PIXEL = PARTICLES_TYPES.register("wisp_pixel",
            () -> new SimpleParticleType(true));
}
