package com.goldbocman.vgm.common.mixin.client;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.registry.custom.PoseType;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Two-handed guns (shotgun, rifle, auto-gun) reuse vanilla's crossbow-hold arm pose instead of hand-tuned Euler
// math in GunAnimationSystem - it's already network-synced and handles every look angle correctly for both the
// local and remote players, which hand-rolled xRot/yRot/zRot formulas kept failing to reproduce.
//? if fabric
@Environment(value = EnvType.CLIENT)
@Mixin(ArmedEntityRenderState.class)
public abstract class ArmedEntityRenderStateMixin {

    @Inject(method = "extractArmedEntityRenderState", at = @At("TAIL"))
    private static void bren$extractArmedEntityRenderState(
            LivingEntity entity,
            ArmedEntityRenderState state,
            ItemModelResolver resolver,
            float partialTick,
            CallbackInfo ci
    ) {
        // Unlike a real crossbow (or the dual-wieldable guns this pattern was copied from), our guns are only ever
        // held in the main hand, but the pose is inherently two-handed. HumanoidModel.poseRightArm/poseLeftArm run
        // as two INDEPENDENT switches - animateCrossbowHold (from the main-hand branch) already sets both arms'
        // rotation correctly, but then the off-hand's own switch runs afterward and, since it's still on its
        // default EMPTY pose (the off-hand genuinely holds nothing), resets that arm's yRot back to 0, undoing
        // half of what was just set. Both poses must be forced together so the off-hand's EMPTY branch never runs.
        if (isTwoHandedGun(state.getMainHandItemStack())) {
            state.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            state.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
    }

    private static boolean isTwoHandedGun(ItemStack stack) {
        return stack != null && stack.getItem() instanceof GunItem gunItem && gunItem.holdingPose() == PoseType.TWO_ARMS;
    }
}
