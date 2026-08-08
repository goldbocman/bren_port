package com.goldbocman.vgm.common.mixin.client.legacy;

//? if <1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack;
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.client.legacy.LegacyGunHeldModels;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Legacy counterpart of ItemRendererMixin/FirstPersonItemRendererMixin/SimpleAnimationMixin, collapsed
// into one clean hook. Calls the existing, version-agnostic GunItem.applyCustomMatrix(...) directly
// instead of the reflection-based ItemStackRenderState.layers hacks the modern mixins fall back to.
//
// ItemInHandRenderer.renderItem is the single choke point for BOTH first- and third-person hand
// rendering on 1.21.1: ItemInHandRenderer.renderHandsWithItems
// (first-person view-hand) and net.minecraft.client.renderer.entity.layers.ItemInHandLayer
// (third-person, added to every LivingEntityRenderer/PlayerRenderer - self AND remote players) both
// call straight into this same method
//
//? if fabric
@Environment(value = EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public abstract class LegacyItemInHandRendererMixin {

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void bren$renderGunAnimation(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
                                          boolean leftHand, PoseStack poseStack, MultiBufferSource buffer,
                                          int light, CallbackInfo ci) {
        if (!(entity instanceof Player player) || !(player instanceof IGunUser gunUser)) return;
        if (stack.isEmpty() || !(stack.getItem() instanceof GunItem gunItem)) return;

        float cooldownProgress = GunApiCompat.getCooldownPercent(player.getCooldowns(), stack, 0.0F);
        GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
        boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);
        boolean mainArmLeft = entity.getMainArm().equals(HumanoidArm.LEFT);

        gunItem.applyCustomMatrix(entity, reloading ? GunHelper.GunStates.RELOADING : GunHelper.GunStates.NORMAL,
                poseStack, stack, cooldownProgress, mainArmLeft);

        if (bren$isHandContext(displayContext)) {
            BakedModel heldModel = LegacyGunHeldModels.resolve(stack);
            if (heldModel != null) {
                Minecraft.getInstance().getItemRenderer().render(stack, displayContext, leftHand, poseStack, buffer,
                        light, OverlayTexture.NO_OVERLAY, heldModel);
                ci.cancel();
            }
        }
    }

    private static boolean bren$isHandContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
*///?}
