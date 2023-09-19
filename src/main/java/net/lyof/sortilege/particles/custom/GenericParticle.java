package net.lyof.sortilege.particles.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class GenericParticle extends TextureSheetParticle {
    public SpriteSet spriteset;
    public static ParticleRenderType TRANSLUCENT = new WispRenderType();

    public GenericParticle(ClientLevel world, double x, double y, double z, float r, float g, float b, SpriteSet spriteset) {
        super(world, x, y, z, 0, 0, 0);
        this.setColor(r, g, b);
        this.setPos(x, y, z);
        this.gravity = 0;
        this.friction = 0f;
        this.lifetime = 10;
        this.spriteset = spriteset;
        this.pickSprite(spriteset);
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TRANSLUCENT;
    }

    @Override
    public void tick() {
        float ratio = (float) (this.getLifetime() - this.age) / this.getLifetime();
        this.setAlpha(ratio);
        this.setSize(this.bbWidth * ratio, this.bbHeight * ratio);
        super.tick();
    }
}
