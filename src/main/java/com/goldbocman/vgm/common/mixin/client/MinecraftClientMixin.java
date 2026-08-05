package com.goldbocman.vgm.common.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import com.goldbocman.vgm.client.AimRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    
    // 使用正确的render方法签名
    @Inject(method = "runTick(Z)V", at = @At("TAIL"))
    private void renderAimOverlay(boolean tick, CallbackInfo ci) {
        // 创建一个矩阵栈用于渲染
        PoseStack matrices = new PoseStack();
        // 使用固定tickDelta值0.0F，因为我们不在渲染循环中
        AimRenderer.renderAimOverlay(matrices, 0.0F);
    }
}