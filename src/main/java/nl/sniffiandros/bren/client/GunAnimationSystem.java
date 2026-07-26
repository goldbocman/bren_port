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

        // Main arm (dominant/shooting hand): raised 85 degrees toward horizontal - absolute value from in-game
        // measurement, not derived from the model transform guesswork of earlier attempts.
        mainArm.xRot = -1.4835299F + p + recoilX - sin;
        mainArm.yRot = (y - bodyYaw) + recoilY;
        mainArm.zRot = isLeftHanded ? -0.1F : 0.1F; // slight roll so the elbow tucks in instead of sticking straight out

        // Support arm: raised 75 degrees - also absolute, measured independently rather than derived from the main
        // arm - and still crossed in toward the main arm's side to grip the foregrip.
        // grip is tapered by cos(p): at neutral pitch (p=0) it's the full ~45 degrees, which looked right - but a
        // FIXED yRot offset combined with a growing pitch (xRot) visually swings further than 45 degrees the more
        // you look up/down (Euler composition, not a bug in the numbers). Scaling it down as |p| grows keeps the
        // apparent crossing angle roughly constant instead of drifting further left the more you look down.
        float grip = (isLeftHanded ? -0.7853982F : 0.7853982F) * (float) Math.cos(p); // ~45 degrees at neutral pitch, tapering toward 0 at extreme up/down
        secondaryArm.xRot = -1.3089969F + p + recoilX - sin;
        secondaryArm.yRot = mainArm.yRot + grip;
        secondaryArm.zRot = isLeftHanded ? 0.15F : -0.15F;

        // Head/hat follow the look direction
        head.yRot = (y - bodyYaw);
        head.xRot = p - sin;
        if (hat != null) {
            hat.yRot = (y - bodyYaw);
            hat.xRot = p - sin;
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