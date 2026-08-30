package net.lyof.sortilege.particle.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class WispParticle extends TextureSheetParticle {
    public WispParticle(ClientLevel clientWorld, double x, double y, double z) {
        super(clientWorld, x, y, z);
    }

    public WispParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet sprite, float r, float g, float b) {
        super(clientWorld, x, y, z);

        this.pickSprite(sprite);

        r = Math.max(0, Math.min(1, r));
        g = Math.max(0, Math.min(1, g));
        b = Math.max(0, Math.min(1, b));
        this.setColor(r, g, b);
        this.gravity = 0;
        this.friction = 0f;
        this.lifetime = 10;

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
    }

    @Override
    public void tick() {
        float ratio = (float) (this.lifetime - this.age) / this.lifetime;
        this.setAlpha(ratio);
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float tint) {
        return 15728880;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteProvider) {
            this.sprites = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(ColorParticleOption parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new WispParticle(world, x, y, z, this.sprites, parameters.getRed(), parameters.getGreen(), parameters.getBlue());
        }
    }
}
