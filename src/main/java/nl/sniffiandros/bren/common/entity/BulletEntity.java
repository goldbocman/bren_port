package nl.sniffiandros.bren.common.entity;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.registry.DamageTypeReg;
import nl.sniffiandros.bren.common.registry.ParticleReg;
import org.jetbrains.annotations.NotNull;

public class BulletEntity extends Projectile {
    private static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BULLET_TYPE = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PUNCH_LEVEL = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);
    private float damage;
    // 子弹类型常量
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_DRAGONBREATH = 1;


    public BulletEntity(EntityType<? extends BulletEntity> entityType, Level world) {
        super(entityType, world);
    }

    public BulletEntity(Level world, float damage, int lifespan, LivingEntity owner, int bulletType) {
        super(Bren.BULLET, world);
        this.damage = damage;
        this.setLifespan(lifespan);
        this.setBulletType(bulletType);
        this.setNoGravity(true);
        this.setOwner(owner);
    }
    public int getLifespan() {
        return this.entityData.get(LIFESPAN);
    }

    public void setLifespan(int lifespan) {
        this.entityData.set(LIFESPAN, lifespan);
    }

    protected void setBulletType(int bulletType) {
        this.entityData.set(BULLET_TYPE, bulletType);
    }
    public int getBulletType() {
        return this.entityData.get(BULLET_TYPE);
    }

    public void setPunchLevel(int punchLevel) {
        this.entityData.set(PUNCH_LEVEL, punchLevel);
    }

    public int getPunchLevel() {
        return this.entityData.get(PUNCH_LEVEL);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFESPAN, 0);
        builder.define(BULLET_TYPE, TYPE_NORMAL);
        builder.define(PUNCH_LEVEL, 0);
    }

    public void tick() {
        float h;
        super.tick();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        boolean bl = false;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            this.level().getBlockState(blockPos);// 修复：使用getEntityWorld()方法替代getWorld()
        }
        if (hitResult.getType() != HitResult.Type.MISS && !bl) {
            this.onHit(hitResult);
        }
        Vec3 vec3d = this.getDeltaMovement();
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        this.updateRotation();
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float g = 0.25f;
                this.level().addParticle(ParticleTypes.BUBBLE, d - vec3d.x * g, e - vec3d.y * g, f - vec3d.z * g, vec3d.x, vec3d.y, vec3d.z); // 修复：使用getEntityWorld()方法替代getWorld()
            }
            h = 0.8f;
        } else {
            h = 0.99f;
        }
        this.setDeltaMovement(vec3d.scale(h));
        if (!this.isNoGravity()) {
            Vec3 vec3d2 = this.getDeltaMovement();
            this.setDeltaMovement(vec3d2.x, vec3d2.y - this.getDefaultGravity(), vec3d2.z);
        }
        this.setPos(d, e, f);

        double l = this.getDeltaMovement().length();

        if (Math.ceil(l) == 0) {
            this.discard();
        }

        if (this.tickCount >= this.getLifespan()) {
            this.discard();
        }

        if (this.level().isClientSide() && this.tickCount >= 2 && this.tickCount % 3 == 0) { // 修复：使用getEntityWorld()方法替代getWorld()
            this.level().addParticle((ParticleOptions) ParticleReg.AIR_RING_PARTICLE, this.getX(), this.getY() + this.getBbHeight()/2, this.getZ(), 0.0, 0.0, 0.0); // 修复：使用getEntityWorld()方法替代getWorld()
        }
    }

    protected double getDefaultGravity() {
        return 0.03;
    }

    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        if (entity.equals(this.getOwner())) {
            return;
        }

        // 重置所有实体的无敌时间
        entity.invulnerableTime = 0;

        // Flame附魔：子弹着火时点燃命中的实体（与onHitBlock中已有的isOnFire()方块点燃逻辑保持一致）
        if (this.isOnFire()) {
            entity.igniteForSeconds(5);
        }

        int punchLevel = this.getPunchLevel();
        if (punchLevel > 0) {
            Vec3 knockback = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(punchLevel * 0.6);
            if (knockback.lengthSqr() > 0.0) {
                entity.push(knockback.x, 0.1, knockback.z);
            }
        }

        DamageSource damageSource = DamageTypeReg.shot(this.level(), this, this.getOwner());
        
        float finalDamage = this.damage;
        
        // 检查是否为龙息弹，如果是则应用特殊效果
        if (this.getBulletType() == TYPE_DRAGONBREATH) {
            // 龙息弹额外伤害
            finalDamage *= 1.2f; // 增加20%伤害
            
            // 对生物实体应用燃烧和发光效果
            if (entity instanceof LivingEntity livingEntity) {
                // 应用燃烧效果：5秒持续时间
                livingEntity.setRemainingFireTicks(200);
                
                // 应用发光效果：10秒持续时间，等级1
                livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // 200 ticks = 10秒
            }
        }

        // 对所有实体类型应用伤害，包括末影水晶、末影龙等
        if (this.level() instanceof ServerLevel serverWorld) {
            entity.hurtServer(serverWorld, damageSource, finalDamage);
        }
        
        this.discard();
    }


    @Override
    protected void onHitBlock(@NotNull BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = this.level().getBlockState(pos); // 修复：使用getEntityWorld()方法替代getWorld()

        Vec3 vec3d = blockHitResult.getLocation();

        if (!state.isAir() && state.isSolid() && this.tickCount > 1) {

            if ((state.is(ConventionalBlockTags.GLASS_BLOCKS) || state.is(ConventionalBlockTags.GLASS_PANES)) && MConfig.bulletsBreakGlass.get()) {
                if (this.level().isClientSide()) { return;} // 修复：使用getEntityWorld()方法替代getWorld()
                this.level().destroyBlock(pos, false, this.getOwner()); // 修复：使用getEntityWorld()方法替代getWorld()
            } else {
                this.level().playSound(null,vec3d.x,vec3d.y,vec3d.z,state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 3.0F); // 修复：使用getEntityWorld()方法替代getWorld()

                boolean isAirAbove = this.level().getBlockState(pos.above()).isAir(); // 修复：使用getEntityWorld()方法替代getWorld()
                boolean hitGround = !this.level().getBlockState(pos).isAir(); // 修复：使用getEntityWorld()方法替代getWorld()

                if (this.level().isClientSide()) { // 修复：使用getEntityWorld()方法替代getWorld()
                    for (int i = 0; i < 4; ++i) {

                        float x = this.random.nextFloat() - .5f;
                        float y = this.random.nextFloat() - .5f;
                        float z = this.random.nextFloat() - .5f;

                        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), vec3d.x,vec3d.y,vec3d.z, x, y, z); // 修复：使用getEntityWorld()方法替代getWorld()
                    }
                } else if (hitGround && isAirAbove && this.isOnFire()){
                    this.level().setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState()); // 修复：使用getEntityWorld()方法替代getWorld()
                }

                this.discard();
            }
        }
    }
}