package net.lyof.sortilege.particles;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.particles.custom.WispParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.antlr.v4.runtime.misc.Triple;

public class ModParticles {
    public static void spawnWisps(ServerLevel world, double x, double y, double z, int amount, Triple<Float, Float, Float> color) {
        //ParticleOptions particle = ConfigEntries.staffsHdParticles ? WISP.get() : WISP_PIXEL.get();
        double spread = amount > 1 ? 0.4 : 0;
        WispParticle.COLOR = color;
        world.sendParticles(WISP_PIXEL.get(), x, y, z, amount, spread, spread, spread, 0);
    }


    public static final DeferredRegister<ParticleType<?>> PARTICLES_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Sortilege.MOD_ID);

    public static void register(IEventBus eventbus) {
        PARTICLES_TYPES.register(eventbus);
    }


    //public static final RegistryObject<SimpleParticleType> WISP = PARTICLES_TYPES.register("wisp",
    //        () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> WISP_PIXEL = PARTICLES_TYPES.register("wisp_pixel",
            () -> new SimpleParticleType(true));
}
