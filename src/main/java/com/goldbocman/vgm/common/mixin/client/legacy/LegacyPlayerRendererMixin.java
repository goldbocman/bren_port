package com.goldbocman.vgm.common.mixin.client.legacy;

//? if <1.21.11 {
/*//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.registry.custom.PoseType;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Legacy counterpart of AvatarRendererMixin. PlayerRenderer.render(...) calls this private static
// getArmPose(...) once per InteractionHand and assigns the result straight onto
// PlayerModel.rightArmPose/leftArmPose before HumanoidModel.setupAnim runs - the exact same authority
// AvatarRenderer.getArmPose has in the render-state world, just under the old
// AbstractClientPlayer/InteractionHand types instead of Avatar/HumanoidArm. Only the player is
// covered (not a general ArmedEntityRenderState-style hook for arbitrary mobs) since every gun in
// this mod is player-held equipment.
//? if fabric
@Environment(value = EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class LegacyPlayerRendererMixin {

    @Inject(
        method = "getArmPose(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void bren$getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.getItem() instanceof GunItem gunItem && gunItem.holdingPose() == PoseType.TWO_ARMS) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
*///?}
