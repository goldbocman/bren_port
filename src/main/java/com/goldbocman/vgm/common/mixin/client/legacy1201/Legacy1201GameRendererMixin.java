package com.goldbocman.vgm.common.mixin.client.legacy1201;

//? if <1.20.5 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Legacy1201 counterpart of LegacyGameRendererMixin - GameRenderer.renderLevel(DeltaTracker)
// doesn't exist yet at 1.20.1; the real vanilla signature there is
// renderLevel(float partialTick, long, PoseStack) instead (confirmed via javap). Otherwise
// identical FOV logic to LegacyGameRendererMixin.
@Mixin(GameRenderer.class)
public class Legacy1201GameRendererMixin {

    @Unique
    private float originalFov = -1.0f;
    @Unique
    private boolean wasAiming = false;

    @Inject(method = "renderLevel",
            //? if forge
            //remap = false,
            at = @At("HEAD"))
    private void bren$modifyFov(float partialTick, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null || !(player instanceof IGunUser gunUser)) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();

        if (!mainHandStack.isEmpty() && mainHandStack.getItem() instanceof GunItem gunItem) {
            boolean isAiming = gunUser.bren_1_21_1$isAiming();

            if (isAiming) {
                if (originalFov == -1.0f) {
                    originalFov = getCurrentFov(client);
                }

                float aimProgress = gunUser.bren_1_21_1$getAimProgress();
                float fovModifier = gunItem.getAimFOVModifier();
                float targetFov = originalFov * fovModifier;

                float smoothedFov = originalFov + (targetFov - originalFov) * aimProgress;
                smoothedFov = Math.max(30, Math.min(110, smoothedFov));

                setFov(client, smoothedFov);
                wasAiming = true;
            } else if (wasAiming) {
                resetFovIfNeeded(client);
            }
        } else if (wasAiming) {
            resetFovIfNeeded(client);
        }
    }

    @Unique
    private void resetFovIfNeeded(Minecraft client) {
        if (wasAiming && originalFov != -1.0f) {
            setFov(client, originalFov);
            originalFov = -1.0f;
            wasAiming = false;
        }
    }

    @Unique
    private float getCurrentFov(Minecraft client) {
        Options options = client.options;
        return options.fov().get();
    }

    @Unique
    private void setFov(Minecraft client, float fov) {
        Options options = client.options;
        options.fov().set((int) fov);
    }
}
*///?}
