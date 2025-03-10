package net.lyof.sortilege.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.lyof.sortilege.Sortilege;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

public class ModParticles {
    public static void spawnWisps(World level, double x, double y, double z, int amount, float[] color) {
        double spread = amount == 1 ? 0 : 1;
        for (int i = 0; i < amount; i++)
            level.addParticle(WISP_PIXEL, x + (0.5 - Math.random()) * spread,
                    y + (0.5 - Math.random()) * spread,
                    z + (0.5 - Math.random()) * spread,
                    color[0], Math.max(0, color[1]), Math.max(0, color[2]));
    }


    public static void register() {}

    public static final DefaultParticleType WISP_PIXEL = Registry.register(Registries.PARTICLE_TYPE, Sortilege.makeID("wisp_pixel"),
            FabricParticleTypes.simple());
}
