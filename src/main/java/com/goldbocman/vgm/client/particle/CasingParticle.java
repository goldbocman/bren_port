package com.goldbocman.vgm.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import com.goldbocman.vgm.common.registry.SoundReg;
import org.jspecify.annotations.Nullable;

public class CasingParticle extends BaseAshSmokeParticle {

    protected float bounce = 0.9f;
    protected boolean made_sound = false;
    protected float angle; // 添加angle字段定义

    protected CasingParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteSet spriteProvider) {
        super(world, x, y, z, 0.1F, 0.1F, 0.1F, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 1.0F, 32, 2.0F, true);
        this.angle = (float) (Math.PI * world.getRandom().nextFloat());
        this.setColor(2.0F,2.0F,2.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.onGround) {
            this.angle += 0.3f;
        } else {
            this.yd += this.bounce;
            this.bounce = 0;
            if (!this.made_sound) {
                this.made_sound = true;

                this.level.playLocalSound(this.x, this.y, this.z, SoundReg.PARTICLE_CASING_BOUNCE, SoundSource.BLOCKS, 1.0F,1.0F - (this.level.getRandom().nextFloat() - 0.5F)/8, false);
            }
        }
    }

    public ParticleRenderType getType() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<ParticleOptions> { // 修改为ParticleEffect
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }
        
        @Override
        public @Nullable Particle createParticle(ParticleOptions parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new CasingParticle(world, x, y, z, velocityX, velocityY, velocityZ, .75F, this.spriteProvider);
        }
    }
}