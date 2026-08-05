package com.goldbocman.vgm.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunHelper;

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
                    case TWO_ARMS -> {
                        // Handled by ArmedEntityRenderStateMixin forcing vanilla's CROSSBOW_HOLD arm pose, which
                        // HumanoidModel.setupAnim already applied before this injection runs - leave it alone
                        // instead of overwriting it with hand-tuned math.
                    }
                    case ONE_ARM ->
                            applyOneArmAnimation(leftArm, rightArm, head, state, entity, cooldownProgress, gunTicks, gunState);
                    case REVOLVER ->
                            applyRevolverAnimation(leftArm, rightArm, head, state, entity, cooldownProgress, gunTicks, gunState);
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
                    case TWO_ARMS -> {
                        // Handled by ArmedEntityRenderStateMixin forcing vanilla's CROSSBOW_HOLD arm pose, which
                        // HumanoidModel.setupAnim already applied before this injection runs - leave it alone
                        // instead of overwriting it with hand-tuned math.
                    }
                    case ONE_ARM ->
                            applyOneArmAnimation(leftArm, rightArm, head, hat, state, entity, cooldownProgress, gunTicks, gunState);
                    case REVOLVER ->
                            applyRevolverAnimation(leftArm, rightArm, head, hat, state, entity, cooldownProgress, gunTicks, gunState);
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
        // Rebuilt to match the revolver pose: driven entirely by vanilla-synced rotation (getXRot/getYHeadRot/
        // getVisualRotationYInDegrees) instead of gunTicks/gunState, which are local-only and never networked to
        // other clients. The old formula collapsed to a fixed "drawn bow" stance for remote players since those
        // fields always read 0 there. This version degrades gracefully instead of getting stuck.
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart mainArm = isLeftHanded ? leftArm : rightArm;
        ModelPart secondaryArm = isLeftHanded ? rightArm : leftArm;
        boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);

        float animationFactor = 0;
        if (entity instanceof Player player) {
            animationFactor = player.getCooldowns().getCooldownPercent(player.getMainHandItem(), 0.0F);
        }
        float sin = reloading ? (float) Math.sin((animationFactor * 2 - 0.5) * Math.PI) * 0.5F + 0.5F : 0;

        // Small local-only recoil/reload flavor - harmless when zeroed out for remote players, unlike the old formula
        float recoilX = (float) (Math.sin(animationFactor * 15) * 0.05235988);
        float recoilY = (float) (Math.cos(animationFactor * 15) * 0.05235988);

        // Vanilla-synced rotation - always correct in third person, for local AND remote players
        float p = entity.getXRot() * 0.01745329F;
        float y = entity.getYHeadRot() * 0.01745329F;
        float bodyYaw = entity.getVisualRotationYInDegrees() * 0.01745329F;

        // Main arm (dominant/shooting hand): 85 degrees was over-raised - combined with the item's own pitch, the
        // wrist ended up almost inside the chest with the elbow collapsed. 72 degrees leaves the elbow visible and
        // the wrist further from the torso; the item transform's pitch was relaxed to match (see *_handheld.json).
        mainArm.xRot = -1.26F + p + recoilX - sin;
        mainArm.yRot = (y - bodyYaw) + recoilY;
        mainArm.zRot = isLeftHanded ? -0.22F : 0.22F; // more roll so the elbow flares naturally instead of pointing straight down

        // Support arm: needs a much bigger pitch gap from the main arm (52 vs 72 degrees, not 75 vs 85) - too close
        // together and both elbows read as parallel instead of the support hand reaching forward to a different
        // point on the weapon. grip is tapered by cos(p) for the same reason as before: a fixed yRot offset swings
        // further than intended as pitch grows, so scaling it down keeps the apparent crossing angle stable.
        float grip = (isLeftHanded ? -0.60F : 0.60F) * (float) Math.cos(p); // ~34 degrees at neutral pitch, tapering toward 0 at extreme up/down
        // xRot follows pitch only partially (0.55x), not 1:1 like the main arm - the support hand is meant to stay
        // near a roughly fixed point on the foregrip, not mirror the aiming arm's full pitch swing. At 1:1 the two
        // arms' pitch sums diverged enough looking down that the support arm ended up pointing below the weapon.
        secondaryArm.xRot = -0.90F + p * 0.55F + recoilX - sin;
        secondaryArm.yRot = mainArm.yRot + grip;
        secondaryArm.zRot = isLeftHanded ? 0.45F : -0.45F; // ~26 degrees roll

        // Head/hat follow the look direction
        head.yRot = (y - bodyYaw);
        head.xRot = p - sin;
        if (hat != null) {
            hat.yRot = (y - bodyYaw);
            hat.xRot = p - sin;
        }
    }

    public static void applyOneArmAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                            HumanoidRenderState state, LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        applyOneArmAnimation(leftArm, rightArm, head, null, state, entity, cooldownProgress, gunTicks, gunState);
    }

    public static void applyOneArmAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                            HumanoidRenderState state, LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        boolean isLeftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);
        ModelPart arm = isLeftHanded ? leftArm : rightArm;

        float h_pi = 1.570796F;
        // Driven by the render state's already-interpolated rotation - the same values vanilla's
        // own setupAnim used for head/hat just before this injection - instead of raw
        // entity.getXRot()/getVisualRotationYInDegrees(), which only update once per tick and
        // produced visible stepping/jerk as the camera rotated.
        float p = state.xRot * 0.017453292F;
        float bodyYaw = state.bodyRot * 0.017453292F;

        arm.yRot = bodyYaw;
        arm.xRot = p - h_pi;

        // Head/hat rotation is left to vanilla's own setupAnim, which already ran before this
        // injection - hat is a child ModelPart of head, so it inherits head's rotation through
        // the parent transform. Setting hat's local rotation to the same absolute value here
        // doubled it, causing the outer skin layer to detach/over-rotate.
    }

    public static void applyRevolverAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head,
                                              HumanoidRenderState state, LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
        applyRevolverAnimation(leftArm, rightArm, head, null, state, entity, cooldownProgress, gunTicks, gunState);
    }

    public static void applyRevolverAnimation(ModelPart leftArm, ModelPart rightArm, ModelPart head, ModelPart hat,
                                              HumanoidRenderState state, LivingEntity entity, float cooldownProgress, int gunTicks, GunHelper.GunStates gunState) {
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

        // Driven by the render state's already-interpolated rotation - the same values vanilla's
        // own setupAnim used for head/hat just before this injection - instead of raw
        // entity.getXRot()/getYHeadRot(), which only update once per tick and produced visible
        // stepping/jerk as the camera rotated. state.yRot is already head yaw relative to body
        // (wrapDegrees(headYaw - bodyRot)), same as the old (y - bodyYaw).
        float p = state.xRot * 0.017453292F;
        float yRelative = state.yRot * 0.017453292F;

        arm.xRot = p - f1 + rotationX;
        arm.yRot = yRelative + rotationY;

        // Head/hat rotation is left to vanilla's own setupAnim, which already ran before this
        // injection - hat is a child ModelPart of head, so it inherits head's rotation through
        // the parent transform. Setting hat's local rotation to the same absolute value here
        // doubled it, causing the outer skin layer to detach/over-rotate. Vanilla also uses
        // interpolated rotation, unlike the raw entity.getXRot()/getYHeadRot() used above, which
        // fixes the jerkiness seen on remote players.
    }
}