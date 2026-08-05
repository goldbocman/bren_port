package com.goldbocman.vgm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.goldbocman.vgm.common.entity.IGunUser;

public class AimRenderer {
    private static final float ZOOM_FOV = 10.0f; // 望远镜缩放FOV

    public static void renderAimOverlay(PoseStack matrices, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || !(player instanceof IGunUser gunUser)) {
            return;
        }

        // 检查是否正在瞄准
        if (gunUser.bren_1_21_1$isAiming() && gunUser.bren_1_21_1$getAimProgress() > 0.5f) {
            renderScopeOverlay(matrices, gunUser.bren_1_21_1$getAimProgress());
        }
    }

    private static void renderScopeOverlay(PoseStack matrices, float progress) {
        // 渲染望远镜瞄准镜效果
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        // 计算缩放效果
        float zoom = 1.0f + (3.0f * progress); // 1x到4x缩放

        // 这里实现望远镜瞄准镜的渲染逻辑
        // 包括黑色边框和中心瞄准镜
    }

}