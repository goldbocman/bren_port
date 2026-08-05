package com.goldbocman.vgm.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public class MuzzleSmokeParticle extends BaseAshSmokeParticle {

    protected MuzzleSmokeParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteSet spriteProvider) {
        super(world, x, y, z, 0.1F, 0.1F, 0.1F, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 1.0F, 2, 0.0F, true);
        this.setColor(1.0F,1.0F,1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        float f = (float) this.age/(this.getLifetime()*2);
        this.setColor(1.0F - f, 1.0F - f, 1.0F - f);
    }

    public ParticleRenderType getType() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    public int getLightColor(float tint) {
        return 15728880;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<ParticleOptions> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable Particle createParticle(ParticleOptions parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new MuzzleSmokeParticle(world, x, y, z, velocityX, velocityY, velocityZ, 3.0F, this.spriteProvider);
        }
    }
}