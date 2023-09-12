package net.lyof.sortilege.particles.custom;

import net.lyof.sortilege.particles.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.antlr.v4.runtime.misc.Triple;
import org.jetbrains.annotations.NotNull;

public class WispParticle extends GenericParticle {
    public static Triple<Float, Float, Float> COLOR = new Triple<>(1f, 1f, 1f);

    public static @NotNull WispParticleProvider provider(SpriteSet spriteSet) {
        return new WispParticleProvider(spriteSet);
    }
    public static class WispParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public WispParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(@NotNull SimpleParticleType typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new WispParticle(worldIn, x, y, z, COLOR.a, COLOR.b, COLOR.c, this.spriteSet);
        }
    }
    
    public WispParticle(ClientLevel world, double x, double y, double z, float r, float g, float b, SpriteSet spriteset) {
        super(world, x, y, z, r, g, b, spriteset);
    }
}
