package nl.sniffiandros.bren.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;
import nl.sniffiandros.bren.common.utils.GunHelper;

/**

 * Gun animation system adapted for the new rendering system in Minecraft 1.21.6

 */
public class GunAnimationSystem {

    /**

     * Apply weapon animation to model parts - compatible with rendering system 1.21.6

     */
    public static void applyGunAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                         HumanoidRenderState state, LivingEntity entity) {
        if (entity instanceof IGunUser gunUser && entity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();

            if (!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem gunItem) {
                ItemCooldowns cooldownManager = player.getCooldowns();
                float cooldownProgress = cooldownManager.getCooldownPercent(mainHandItem, 0.0F);

                GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
                int gunTicks = gunUser.bren_1_21_1$getGunTicks();

                switch (gunItem.holdingPose()) {
                    case TWO_ARMS ->
                            applyTwoArmsAnimation(leftArm, rightArm, head, entity, cooldownProgress, gunTicks, gunState);
                    case ONE_ARM ->
                            applyOneArmAnimation(leftArm, rightArm, head, entity, cooldownProgress, gunTicks, gunState);
                    case REVOLVER ->
                            applyRevolverAnimation(leftArm, rightArm, head, entity, cooldownProgress, gunTicks, gunState);
                }
            }
        }
    }

    public static void applyGunAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                         HumanoidRenderState state, LivingEntity entity) {
        if (entity instanceof IGunUser gunUser && entity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();

            if (!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem gunItem) {
                ItemCooldowns cooldownManager = player.getCooldowns();
                float cooldownProgress = cooldownManager.getCooldownPercent(mainHandItem, 0.0F);

                GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
                int gunTicks = gunUser.bren_1_21_1$getGunTicks();

                switch (gunItem.holdingPose()) {
                    case TWO_ARMS ->
                            applyTwoArmsAnimation(leftArm, rightArm, head, hat, entity, cooldownProgress, gunTicks, gunState);
                    case ONE_ARM ->
                            applyOneArmAnimation(leftArm, rightArm, head, hat, entity, cooldownProgress, gunTicks, gunState);
                    case REVOLVER ->
                            applyRevolverAnimation(leftArm, rightArm, head, hat, entity, cooldownProgress, gunTicks, gunState);
                }
            }
        }
    }

    public static void applyTwoArmsAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                             LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        applyTwoArmsAnimation(leftArm, rightArm, head, null, entity, cooldownProgress, gunTicks, gunState);
    }

    public static void applyTwoArmsAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                             LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart mainArm = isLeftHanded ? leftArm : rightArm;
        ModelPart secondaryArm = isLeftHanded ? rightArm : leftArm;

        float animationProgress = Math.max(cooldownProgress - 0.15F, 0);
        boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);
        float kick = 2.5F;

        animationProgress = Math.max(animationProgress - 0.15F, 0);
        float f = (((float)gunTicks/16) + animationProgress)/2;
        float f1 = (float) (Math.sin(f)/Math.PI) * (kick/2);

        float l = isLeftHanded ? -1 : 1;

        // Get entity angle (convert to radians)
        float p = entity.getXRot() * 0.01745329F;
        float y = entity.getYHeadRot() * 0.01745329F;
        float bodyYaw = entity.getVisualRotationYInDegrees() * 0.01745329F;

        float f2 = f1*kick/2;

        float fr = ((float) Math.sin((animationProgress * 2 - 0.5) * Math.PI) * 0.5F + 0.5F);
        float f3 = reloading ? fr/4 : f2 ;
        float f4 = reloading ? (isLeftHanded ? -fr/4 : fr/4) : f2 * l;

        // Main arm animation - relative to torso
        mainArm.yRot = isLeftHanded ? (y - bodyYaw) + 0.7853982F : (y - bodyYaw) - 0.7853982F;
        mainArm.xRot = 0.2181662F + p + f3/2;
        mainArm.zRot += f4;

        // Off-arm animation - relative to torso
        secondaryArm.xRot = -0.6981317F + p/3 - f3/2 - (reloading ? fr:0);
        secondaryArm.yRot = (isLeftHanded ? -1.090831F - (y - bodyYaw) : 1.090831F + (y - bodyYaw)) + (p/2) * l + f3/3;

        // Head animation - relative to torso
        head.yRot = (y - bodyYaw);
        head.xRot = p - f3/2;

        // Hat layer animation - synchronized with the head
        if (hat != null) {
            hat.yRot = (y - bodyYaw);
            hat.xRot = p - f3/2;
        }
    }

    public static void applyOneArmAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                            LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        applyOneArmAnimation(leftArm, rightArm, head, null, entity, cooldownProgress, gunTicks, gunState);
    }

    public static void applyOneArmAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                            LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart arm = isLeftHanded ? leftArm : rightArm;

        float h_pi = 1.570796F;
        float p = entity.getXRot() * 0.01745329F;
        float bodyYaw = entity.getVisualRotationYInDegrees() * 0.01745329F;

        arm.yRot = bodyYaw;
        arm.xRot = p - h_pi;

        head.yRot = 0;
        head.xRot = p;

        // Hat layer animation - synchronized with the head
        if (hat != null) {
            hat.yRot = 0;
            hat.xRot = p;
        }
    }

    public static void applyRevolverAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                              LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        applyRevolverAnimation(leftArm, rightArm, head, null, entity, cooldownProgress, gunTicks, gunState);
    }

    public static void applyRevolverAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                              LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart arm = isLeftHanded ? leftArm : rightArm;
        boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);

        float rotationX;
        float rotationY;
        float animationFactor = 0;
        float f1 = 1.570796F;

        if (entity instanceof Player player) {
            animationFactor = player.getCooldowns().getCooldownPercent(player.getMainHandItem(), 0.0F);
        }

        float sin = reloading ? (float) Math.sin((animationFactor*2 - 0.5)*Math.PI) * 0.5F + 0.5F : 0;

        rotationY = (float) (Math.cos(animationFactor*15)*0.08726646);
        rotationX = (float) (Math.sin(animationFactor*15)*0.08726646) - sin;

        float p = entity.getXRot() * 0.01745329F;
        float y = entity.getYHeadRot() * 0.01745329F;
        float bodyYaw = entity.getVisualRotationYInDegrees() * 0.01745329F;

        arm.xRot = p - f1 + rotationX;
        arm.yRot = (y - bodyYaw) + rotationY;

        head.yRot = (y - bodyYaw);
        head.xRot = p - sin;

        // Hat layer animation - synchronized with the head
        if (hat != null) {
            hat.yRot = (y - bodyYaw);
            hat.xRot = p - sin;
        }
    }
}