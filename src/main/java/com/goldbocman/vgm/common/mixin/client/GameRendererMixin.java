package com.goldbocman.vgm.common.mixin.client;

//? if >=1.21.11 {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    private GameRenderState gameRenderState;

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
                    System.out.println("[Bren FOV] Original FOV saved: " + originalFov);
                }

                float aimProgress = gunUser.bren_1_21_1$getAimProgress();
                float fovModifier = gunItem.getAimFOVModifier();
                float targetFov = originalFov * fovModifier;

                // 平滑的FOV过渡
                float smoothedFov = originalFov + (targetFov - originalFov) * aimProgress;

                // 确保FOV在有效范围内 (通常30-110)
                smoothedFov = Math.max(30, Math.min(110, smoothedFov));

                System.out.println("[Bren FOV] Aiming: progress=" + aimProgress +
                        ", modifier=" + fovModifier +
                        ", target=" + targetFov +
                        ", setting=" + smoothedFov);

                // 设置FOV
                setFov(client, smoothedFov);
                wasAiming = true;
            } else {
                if (wasAiming) {
                    System.out.println("[Bren FOV] Stopped aiming, resetting FOV to: " + originalFov);
                    resetFovIfNeeded(client);
                }
            }
        } else {
            if (wasAiming) {
                System.out.println("[Bren FOV] No gun in hand, resetting FOV to: " + originalFov);
                resetFovIfNeeded(client);
            }
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

    @Unique
    public GameRenderState getGameRenderState() {
        return gameRenderState;
    }
}
//?}