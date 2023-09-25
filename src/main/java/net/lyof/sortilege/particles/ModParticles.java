package net.lyof.sortilege.particles;

import net.lyof.sortilege.Sortilege;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Sortilege.MOD_ID);

    public static void register(IEventBus eventbus) {
        PARTICLES_TYPES.register(eventbus);
    }


    public static final RegistryObject<SimpleParticleType> WISP = PARTICLES_TYPES.register("wisp",
            () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> WISP_PIXEL = PARTICLES_TYPES.register("wisp_pixel",
            () -> new SimpleParticleType(true));
}
