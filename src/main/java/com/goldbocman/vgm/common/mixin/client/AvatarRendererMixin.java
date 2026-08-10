package com.goldbocman.vgm.common.mixin.client;

//? if >=1.21.11 {
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.registry.custom.PoseType;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// AvatarRenderer.extractRenderState runs HumanoidMobRenderer.extractHumanoidRenderState (which
// ArmedEntityRenderStateMixin targets) and then unconditionally OVERWRITES rightArmPose/leftArmPose right
// afterward via this exact method - that's the real, final authority on a player's third-person arm pose, and
// it only recognizes vanilla items (CrossbowItem, bow, trident, ...), falling through to the generic ITEM pose
// for anything else. ArmedEntityRenderStateMixin's change was getting silently clobbered by this.
//? if fabric
@Environment(value = EnvType.CLIENT)
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Inject(
            method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bren$getArmPose(Avatar avatar, HumanoidArm arm, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack mainHandItem = avatar.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.getItem() instanceof GunItem gunItem && gunItem.holdingPose() == PoseType.TWO_ARMS) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
//?}
