package com.goldbocman.vgm.common.mixin.client.legacy;

//? if <1.21.11 {
/*import net.minecraft.client.DeltaTracker;
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

// Legacy counterpart of GameRendererMixin. GameRenderer.renderLevel(DeltaTracker) still exists
// pre-1.21.11, so the injection point is unchanged - this only drops the @Shadow GameRenderState
// gameRenderState/getGameRenderState() accessor, since that class doesn't exist yet and the FOV logic
// below never touched it anyway (it only reads/writes client.options.fov()).
@Mixin(GameRenderer.class)
public class LegacyGameRendererMixin {

    @Unique
    private float originalFov = -1.0f;
    @Unique
    private boolean wasAiming = false;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void bren$modifyFov(DeltaTracker deltaTracker, CallbackInfo ci) {
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
