package com.goldbocman.vgm.client.legacy;

//? if <1.21.11 {
/*import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;

// Pre-render-state counterpart of GunAnimationSystem. HumanoidModel.setupAnim(T entity, float
// limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) hands the
// entity straight to the caller, and netHeadYaw/headPitch are already the same interpolated,
// body-relative values the render-state world exposes as state.yRot/state.xRot - so this takes those
// two floats directly instead of a render-state object, with no other behavior change.
public class LegacyGunAnimationSystem {

    public static void applyGunAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                          LivingEntity entity, float netHeadYaw, float headPitch) {
        if (entity instanceof IGunUser gunUser && entity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();

            if (!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem gunItem) {
                ItemCooldowns cooldownManager = player.getCooldowns();
                float cooldownProgress = GunApiCompat.getCooldownPercent(cooldownManager, mainHandItem, 0.0F);

                GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();

                switch (gunItem.holdingPose()) {
                    case TWO_ARMS -> {
                        // Handled by LegacyPlayerRendererMixin forcing vanilla's CROSSBOW_HOLD arm pose before
                        // HumanoidModel.setupAnim applies it - leave it alone here, same as the modern system.
                    }
                    case ONE_ARM ->
                            applyOneArmAnimation(leftArm, rightArm, entity, headPitch);
                    case REVOLVER ->
                            applyRevolverAnimation(leftArm, rightArm, entity, netHeadYaw, headPitch, cooldownProgress, gunState);
                }
            }
        }
    }

    // Not reachable from any currently-shipped gun's holdingPose() - see gun-rendering.md. Ported
    // for parity with GunAnimationSystem.
    public static void applyOneArmAnimation(ModelPart leftArm, ModelPart rightArm, LivingEntity entity, float headPitch) {
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart arm = isLeftHanded ? leftArm : rightArm;

        float h_pi = 1.570796F;
        float p = headPitch * 0.017453292F;

        arm.yRot = 0;
        arm.xRot = p - h_pi;
    }

    public static void applyRevolverAnimation(ModelPart leftArm, ModelPart rightArm, LivingEntity entity,
                                               float netHeadYaw, float headPitch, float cooldownProgress,
                                               GunHelper.GunStates gunState) {
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart arm = isLeftHanded ? leftArm : rightArm;
        boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);

        float rotationX;
        float rotationY;
        float f1 = 1.570796F;

        float sin = reloading ? (float) Math.sin((cooldownProgress * 2 - 0.5) * Math.PI) * 0.5F + 0.5F : 0;

        rotationY = (float) (Math.cos(cooldownProgress * 15) * 0.08726646);
        rotationX = (float) (Math.sin(cooldownProgress * 15) * 0.08726646) - sin;

        float p = headPitch * 0.017453292F;
        float yRelative = netHeadYaw * 0.017453292F;

        arm.xRot = p - f1 + rotationX;
        arm.yRot = yRelative + rotationY;
    }
}
*///?}
