package nl.sniffiandros.bren.common.utils;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.entity.BulletEntity;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.network.NetworkUtils;
import nl.sniffiandros.bren.common.registry.ItemReg;
import nl.sniffiandros.bren.common.registry.NetworkReg;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;
import nl.sniffiandros.bren.common.registry.custom.types.*;

import java.util.ArrayList;
import java.util.List;


public class GunUtils {

    public static int fire(LivingEntity user) {

        Level world = user.level();
        ItemStack stack = user.getMainHandItem();
        IGunUser gunUser = (IGunUser)user;
    
        if (!(stack.getItem() instanceof GunItem gunItem)) return 0;
    
        // 关键修复：直接使用GunItem的getGunProperties方法获取属性，而不是从玩家属性实例获取
        int fireRate = 0;
        GunProperties properties = gunItem.getGunProperties(stack);
        if (properties != null) {
            fireRate = properties.fireRate;
        }
        
        // 如果属性值为0，使用默认值
        if (fireRate <= 0) {
            fireRate = 3; // 使用默认射速
        }

        // 使用正常的射击声音
        SoundEvent shootSound = gunItem.getShootSound(stack);
        
        // 只有在声音不为null时才播放
        if (shootSound != null) {
            world.playSound(null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    shootSound,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (!world.isClientSide()) {

            if (user.isAlwaysTicking()) {
                // 使用EquipmentSlot参数的正确damage方法
                stack.hurtAndBreak(1, user, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }

            List<Vec3> position = GunUtils.calculatePositionBasedOnAngle(user);
            Vec3 origin = position.get(0);
            Vec3 front = position.get(1);
            Vec3 down = position.get(2);
            Vec3 side = position.get(3);

            for (Player p : user.level().players()) {
                NetworkUtils.sendShotEffect(p, origin.add(side.add(down).scale(0.15)), front, gunItem.ejectCasing());
            }

            for (int i = 0; i < gunItem.bulletAmount(); ++i) {
                float x = (user.getRandom().nextFloat() - .5f) * 2 * gunItem.spread();
                float y = (user.getRandom().nextFloat() - .5f) * 2 * gunItem.spread();

                if (gunItem instanceof ShotgunItem) {
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2(x,y), 0, gunItem.bulletSpeed(stack), gunItem.bulletLifespan());
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2(x,y), 0, gunItem.bulletSpeed(stack), gunItem.bulletLifespan());
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2(x,y), 0, gunItem.bulletSpeed(stack), gunItem.bulletLifespan());
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2(x,y), 0, gunItem.bulletSpeed(stack), gunItem.bulletLifespan());
                } else {
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2(x,y), 0, gunItem.bulletSpeed(stack), gunItem.bulletLifespan());
                }
            }

            if (gunItem instanceof FlareGunItem) {
                Vec3 eyePos = new Vec3(user.getX(), user.getEyeY(), user.getZ());

                net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(
                        eyePos.x - 40, eyePos.y - 40, eyePos.z - 40,
                        eyePos.x + 40, eyePos.y + 40, eyePos.z + 40
                );

                java.util.List<net.minecraft.world.entity.LivingEntity> entities = world.getEntitiesOfClass(
                        net.minecraft.world.entity.LivingEntity.class, searchBox,
                        entity -> entity != null && entity.isAlive()
                );

                for (int j = 0; j < 20; ++j) {
                    double t = Math.pow(world.getRandom().nextFloat(), 1.5);
                    Vec3 p = origin.add(front.scale(0.8 + t));
                    Vec3 v = front.scale(0.1 * (1 - t)).add(0, 0.1, 0);
                    world.addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, v.x, v.y, v.z);
                }

                for (net.minecraft.world.entity.LivingEntity entity : entities) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.GLOWING, 200, 0));
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            MobEffects.NIGHT_VISION, 200, 0));
                }
            }
        }

        if (user instanceof Player player) {
            FriendlyByteBuf buf = FriendlyByteBufs.create();

            // 关键修复：直接使用GunItem的getGunProperties方法获取后坐力属性
            float recoil = 0f;
                if (properties != null) {
                    recoil = properties.recoil;
                }
            

            // 新增：调用CalculateRecoil函数计算后坐力
            double calculatedRecoil = CalculateRecoil(player, stack, recoil);
            recoil = (float) calculatedRecoil;

            buf.writeFloat(recoil);

            NetworkUtils.sendDataToClient(player, NetworkReg.RECOIL_CLIENT_PACKET_ID.id(), buf);
        }

        gunItem.useBullet(stack);

        return fireRate;
    }

    public static double CalculateRecoil(Player player, ItemStack ignoredStack, double baseRecoil) {
        baseRecoil *= MConfig.recoilMultiplier.get();
        if (player.isCrouching()) {
            baseRecoil *= 0.5;
        }
        return Math.round(baseRecoil * 2) / 2.0;
    }

    public static List<Vec3> calculatePositionBasedOnAngle(LivingEntity entity) {
        Vec3 front = Vec3.directionFromRotation(entity.getXRot(), entity.getYRot());
        HumanoidArm arm = entity.getMainArm();
        boolean isRightHand = arm == HumanoidArm.RIGHT;
        Vec3 side = Vec3.directionFromRotation(0, entity.getYRot() + (isRightHand ? 90 : -90));
        Vec3 down = Vec3.directionFromRotation(entity.getXRot() + 90, entity.getYRot());

        Vec3 origin = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

        List<Vec3> positions = new ArrayList<>();
        positions.add(origin);
        positions.add(front);
        positions.add(down);
        positions.add(side);
        return positions;
    }

    public static void spawnBullet(LivingEntity entity, Vec3 origin, Vec3 front, ItemStack stack, Vec2 spread,
                                   int ticksOnFire, float speed, int lifespan) {

        Level world = entity.level();

        // 关键修复：直接使用GunItem的getGunProperties方法获取属性，而不是从玩家属性实例获取
        float rangedDamage = 0f;
        if (stack.getItem() instanceof GunItem gunItem) {
            GunProperties properties = gunItem.getGunProperties(stack);
            if (properties != null) {
                rangedDamage = properties.rangedDamage;
            }
        }

        // 如果属性值为0，使用默认值
        if (rangedDamage <= 0) {
            rangedDamage = 10.0f; // 使用默认伤害
        }

        // 检测子弹类型：检查当前使用的子弹是否为龙息弹
        int bulletType = BulletEntity.TYPE_NORMAL;
        if (stack.getItem() instanceof GunItem gunItem) {
            // 检查霰弹枪是否使用龙息弹
            if (gunItem instanceof ShotgunItem) {
                // 检查玩家当前使用的子弹类型
                ItemStack mainHandItem = entity.getMainHandItem();
                if (!mainHandItem.isEmpty() && mainHandItem.getItem() == ItemReg.DRAGONBREATH_SHELL) {
                    bulletType = BulletEntity.TYPE_DRAGONBREATH;
                }
            }
        }

        // 检测子弹类型：从霰弹枪物品栈中读取存储的子弹类型
        if (stack.getItem() instanceof ShotgunItem shotgunItem) {
            int optionalBulletType = shotgunItem.getCurrentBulletType(stack);
            if (optionalBulletType != 0) {
                bulletType = optionalBulletType == 1 ? BulletEntity.TYPE_DRAGONBREATH : BulletEntity.TYPE_NORMAL;
            }
        }

        BulletEntity bullet = new BulletEntity(world, rangedDamage, lifespan, entity, bulletType);

        // 新增：根据枪械类型设置不同的子弹属性
        if (stack.getItem() instanceof GunItem gunItem) {
            // 检查是否为霰弹枪
            if (gunItem instanceof ShotgunItem) {
                // 霰弹枪子弹的特殊处理：增加散射效果
                float shotgunSpread = 5.0f; // 霰弹散射角度
                spread = new Vec2(
                        spread.x + (entity.getRandom().nextFloat() - 0.5f) * shotgunSpread,
                        spread.y + (entity.getRandom().nextFloat() - 0.5f) * shotgunSpread
                );

                // 霰弹枪子弹的特殊属性：降低速度，增加重力
                speed *= 0.8f; // 降低速度
                bullet.setNoGravity(false); // 启用重力
            }

            // 检查是否为机枪
            if (gunItem instanceof MachineGunItem) {
                // 机枪子弹的特殊处理：增加射程
                lifespan = (int)(lifespan * 1.2f); // 增加20%射程
            }
        }

        Vec3 bulletPos = origin.subtract(new Vec3(0,0.2,0)).subtract(front.scale(speed));

        bullet.setPosRaw(bulletPos.x(), bulletPos.y() - 0.1, bulletPos.z());
        bullet.shootFromRotation(entity, entity.getXRot() + spread.y, entity.getYHeadRot() + spread.x, 0.0F, speed, 0.0F);

        // 修复：移除velocityModified字段的赋值，只保留velocityDirty
        bullet.needsSync = true;
        bullet.setRemainingFireTicks(ticksOnFire);

        world.addFreshEntity(bullet);
    }


    public static void sendAnimationPacket(Player player) {
        FriendlyByteBuf buf = FriendlyByteBufs.empty();

        NetworkUtils.sendDataToClient(player, NetworkReg.SHOOT_ANIMATION_PACKET_ID.id(), buf);
    }

    public static void playDistantGunFire(Level world, Vec3 pos) {
        if (world.isClientSide()) {
            return;
        }

        world.players().forEach(player -> {
            double distance = player.distanceToSqr(pos);

            if (distance > 60) {

                float volume = (float) Math.max(1.0F - (distance / 400)/100, 0);

                if (volume > 0) {
                    FriendlyByteBuf buf = FriendlyByteBufs.create();
                    buf.writeFloat(volume);
                    NetworkUtils.sendDataToClient(player, NetworkReg.SHOOT_CLIENT_PACKET_ID.id(), buf);
                }
            }
        });
    }


    public static void fillMagazine(ItemStack mag, Player player) {
        if (!(mag.getItem() instanceof MagazineItem)) {
            return;
        }
        
        while (true) {
            ItemStack bulletStack = Bren.getItemFromPlayer(player, ItemReg.BULLET);

            if (bulletStack.isEmpty()) {
                break;
            }
            
            int currentContents = MagazineItem.getContents(mag);
            int maxCapacity = MagazineItem.getMaxCapacity(mag);
            
            if (currentContents >= maxCapacity) {
                break;
            }
            
            int filled = MagazineItem.fillMagazine(mag, 1); // 每次填入1发
            if (filled > 0) {
                bulletStack.shrink(filled);
            } else {
                break;
            }
        }
    }
}