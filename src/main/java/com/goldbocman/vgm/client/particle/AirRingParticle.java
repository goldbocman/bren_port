package com.goldbocman.vgm.client.particle;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
//? if >=26.1 {
import org.jspecify.annotations.Nullable;
//?} else {
/*import org.jetbrains.annotations.Nullable;
*///?}

public class AirRingParticle extends BaseAshSmokeParticle {

    protected AirRingParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteSet spriteProvider) {
        super(world, x, y, z, 0.1F, 0.1F, 0.1F, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 1.0F, 1, 0.0F, true);
        this.setColor(2.0F,2.0F,2.0F);
    }


    public ParticleRenderType getType() {
        //? if >=1.21.11 {
        return ParticleRenderType.SINGLE_QUADS;
        //?} else {
        /*return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        *///?}
    }

    public int getLightColor(float tint) {
        return 15728880;
    }

    //? if fabric
    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<ParticleOptions> { // 修改为ParticleEffect
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        //? if >=1.21.11 {
        @Override
        public @Nullable Particle createParticle(ParticleOptions parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new AirRingParticle(world, x, y, z, velocityX, velocityY, velocityZ, 1.8F, this.spriteProvider);
        }
        //?} else {
        /*@Override
        public @Nullable Particle createParticle(ParticleOptions parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new AirRingParticle(world, x, y, z, velocityX, velocityY, velocityZ, 1.8F, this.spriteProvider);
        }
        *///?}
    }
}