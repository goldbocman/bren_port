package com.goldbocman.vgm.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunHelper;

/**
 * 适配Minecraft 1.21.4新渲染系统的枪械模型动画器
 */
public class GunEntityModelAnimator {
    
    /**
     * 新的动画方法，适配BipedEntityRenderState
     */
    public static void applyAnimation(HumanoidRenderState state, ModelPart leftArm, ModelPart rightArm, 
                                     ModelPart head, LivingEntity entity, float cooldownProgress, 
                                     int gunTicks, GunHelper.GunStates gunState) {
        if (entity instanceof IGunUser) {
            // 根据实体信息应用动画
            applyAnimationToParts(leftArm, rightArm, head, entity, cooldownProgress, gunTicks, gunState);
        }
    }
    
    private static void applyAnimationToParts(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                             LivingEntity entity, float cooldownProgress, 
                                             int gunTicks, GunHelper.GunStates gunState) {
        // 原有的动画逻辑可以在这里重用
        // 但需要适配新的渲染系统限制
        
        if (entity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            
            if (!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem gunItem) {
                // 根据持枪姿势应用不同的动画
                switch (gunItem.holdingPose()) {
                    case TWO_ARMS -> 
                        angles(entity, 0, 0, 0, entity.getYHeadRot(), entity.getXRot(), 
                              leftArm, rightArm, head, cooldownProgress);
                    case ONE_ARM -> 
                        oneArm(entity, 0, 0, 0, entity.getYHeadRot(), entity.getXRot(), 
                              leftArm, rightArm, head, cooldownProgress);
                    case REVOLVER -> 
                        revolver(entity, 0, 0, 0, entity.getYHeadRot(), entity.getXRot(), 
                               leftArm, rightArm, head, cooldownProgress);
                }
            }
        }
    }
    
    // 保留原有的静态方法供兼容性使用
    public static void oneArm(LivingEntity livingEntity, float ignoredLimbSwing, float ignoredLimbSwingAmount, 
                             float ignoredAgeInTicks, float netHeadYaw, float headPitch,
                             ModelPart leftArm, ModelPart rightArm, ModelPart ignoredHead, float ignoredGunAmount) {
        if (livingEntity instanceof IGunUser && !livingEntity.isSleeping()) {
            boolean isLeftHanded = livingEntity.getMainArm().equals(HumanoidArm.LEFT);

            float h_pi = 1.570796F;
            float p = headPitch * 0.01745329F;
            float y = netHeadYaw * 0.01745329F;

            ModelPart arm = isLeftHanded ? leftArm : rightArm;

            arm.yRot = y;
            arm.xRot = p - h_pi;
        }
    }
    
    public static void revolver(LivingEntity livingEntity, float ignoredLimbSwing, float ignoredLimbSwingAmount,
                               float ignoredAgeInTicks, float netHeadYaw, float headPitch,
                               ModelPart leftArm, ModelPart rightArm, ModelPart ignoredHead, float ignoredGunAmount) {
        if (livingEntity instanceof IGunUser gunUser && !livingEntity.isSleeping()) {
            boolean reloading = gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING);

            boolean isLeftHanded = livingEntity.getMainArm().equals(HumanoidArm.LEFT);
            ModelPart arm = isLeftHanded ? leftArm : rightArm;

            float rotX;
            float rotY;
            float f = 0;
            float f1 = 1.570796F;

            if (livingEntity instanceof Player player) {

                // 修复：getCooldownProgress需要2个参数（物品栈和部分刻）
                f = player.getCooldowns().getCooldownPercent(player.getMainHandItem(), 0.0F);
            }

            float sin = reloading ? (float) Math.sin((f*2 - 0.5)*Math.PI) * 0.5F + 0.5F : 0;

            rotY = (float) (Math.cos(f*15)*0.08726646);
            rotX = (float) (Math.sin(f*15)*0.08726646) - sin;

            float p = headPitch * 0.01745329F;
            float y = netHeadYaw * 0.01745329F;

            arm.xRot = p - f1 + rotX;
            arm.yRot = y + rotY;
        }
    }

    public static void angles(LivingEntity livingEntity, float ignoredLimbSwing, float ignoredLimbSwingAmount,
                             float ignoredAgeInTicks, float netHeadYaw, float headPitch,
                             ModelPart leftArm, ModelPart rightArm, ModelPart head, float gunAmount) {

        if (livingEntity instanceof IGunUser gunUser && !livingEntity.isSleeping()) {

            boolean reloading = gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING);
            float kick = 2.5F;

            gunAmount = Math.max(gunAmount - 0.15F, 0);
            float f = (((float)gunUser.bren_1_21_1$getGunTicks()/16) + gunAmount)/2;
            float f1 = (float) (Math.sin(f)/Math.PI) * (kick/2);


            boolean isLeftHanded = livingEntity.getMainArm().equals(HumanoidArm.LEFT);
            ModelPart arm = isLeftHanded ? leftArm : rightArm;
            ModelPart other_arm = !isLeftHanded ? leftArm : rightArm;

            float l = isLeftHanded ? -1 : 1;

            float p = headPitch * 0.01745329F;
            float y = netHeadYaw * 0.01745329F;

            float f2 = f1*kick/2;

            float fr = ((float) Math.sin((gunAmount * 2 - 0.5) * Math.PI) * 0.5F + 0.5F);
            float f3 = reloading ? fr/4 : f2 ;
            float f4 = reloading ? (isLeftHanded ? -fr/4 : fr/4) : f2 * l;

            arm.yRot = isLeftHanded ? y + 0.7853982F : y - 0.7853982F;
            arm.xRot = 0.2181662F + p + f3/2;
            arm.zRot += f4;

            other_arm.xRot = -0.6981317F + p/3 - f3/2 - (reloading ? fr:0);
            other_arm.yRot = (isLeftHanded ? -1.090831F - y : 1.090831F + y) + (p/2) * l + f3/3;

            head.yRot = y - 0.7853982F * l;

        }
    }
}