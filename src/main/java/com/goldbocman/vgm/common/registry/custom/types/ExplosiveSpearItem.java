package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplosiveSpearItem extends Item {

    /**
     * The explosion power of this spear.
     */
    private final float explosionPower;

    /**
     * The tier of this spear.
     */
    /**
     * The explosion power of this spear.
     */
    /**
     * The tier of this spear.
     */
    private final ToolMaterial tier;


    public ExplosiveSpearItem(ToolMaterial tier, float explosionPower, Properties properties) {
        super(properties);
        this.tier = tier;
        this.explosionPower = explosionPower;
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        // 所有攻击都触发爆炸效果
        triggerExplosion(target.level(), target.position(), attacker);

        super.hurtEnemy(stack, target, attacker);
    }

    private void triggerExplosion(Level level, Vec3 position, @Nullable LivingEntity attacker) {
        if (level.isClientSide()) return;

        // 播放爆炸声音
        level.playSound(null, position.x, position.y, position.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F,
                (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);

        // 创建爆炸效果
        level.explode(attacker,
                attacker != null ? attacker.damageSources().explosion(attacker, attacker) : null,
                null,
                position.x, position.y, position.z,
                explosionPower, false, Level.ExplosionInteraction.MOB);

        // 添加粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = (level.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 2.0;

            level.addParticle(ParticleTypes.EXPLOSION,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0, 0);
        }
    }


    public ToolMaterial getTier() {
        return tier;
    }
}