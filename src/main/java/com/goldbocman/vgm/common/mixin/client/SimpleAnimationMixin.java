package com.goldbocman.vgm.common.mixin.client;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 简单的动画测试Mixin
 * 用于调试和验证动画系统是否正常工作
 */
@SuppressWarnings("ALL")
//? if fabric
@Environment(value= EnvType.CLIENT)
@Mixin(SpecialModelWrapper.class)
public class SimpleAnimationMixin {

    @Inject(at = @At("HEAD"), method = "update")
    private void bren$simpleAnimationTest(
            ItemStackRenderState output, 
            ItemStack item, 
            net.minecraft.client.renderer.item.ItemModelResolver resolver, 
            ItemDisplayContext displayContext, 
            net.minecraft.client.multiplayer.ClientLevel level, 
            net.minecraft.world.entity.ItemOwner owner, 
            int seed, 
            CallbackInfo ci) {
        
        Minecraft client = Minecraft.getInstance();

        // 只在客户端且玩家存在时处理
        if (client.player != null && client.getCameraEntity() instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
            ItemStack mainHandItem = client.player.getMainHandItem();
            ItemStack offHandItem = client.player.getOffhandItem();

            // 检查是否为枪械物品
            boolean isMainHandGun = !mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem;
            boolean isOffHandGun = !offHandItem.isEmpty() && offHandItem.getItem() instanceof GunItem;

            // 调试信息
            if (isMainHandGun || isOffHandGun) {
                System.out.println("[Bren Simple Debug] Gun detected in hand - Display Context: " + displayContext);
                
                // 尝试简单的动画测试
                try {
                    // 方法1：尝试使用 pose() 方法
                    try {
                        java.lang.reflect.Method poseMethod = output.getClass().getDeclaredMethod("pose");
                        com.mojang.blaze3d.vertex.PoseStack poseStack = (com.mojang.blaze3d.vertex.PoseStack) poseMethod.invoke(output);
                        
                        if (poseStack != null) {
                            // 应用简单的平移变换
                            poseStack.translate(0, 0, 0.2F);
                            System.out.println("[Bren Simple Debug] Simple translation applied via pose() method");
                        }
                    } catch (Exception e) {
                        System.err.println("[Bren Simple Debug] pose() method failed: " + e.getMessage());
                    }
                    
                    // 方法2：尝试直接访问变换字段
                    try {
                        java.lang.reflect.Field transformField = output.getClass().getDeclaredField("transform");
                        transformField.setAccessible(true);
                        org.joml.Matrix4f transform = (org.joml.Matrix4f) transformField.get(output);
                        
                        if (transform != null) {
                            // 应用简单的平移
                            transform.translate(0, 0, 0.2F);
                            System.out.println("[Bren Simple Debug] Simple translation applied via transform field");
                        }
                    } catch (Exception e) {
                        System.err.println("[Bren Simple Debug] transform field access failed: " + e.getMessage());
                    }
                    
                } catch (Exception e) {
                    System.err.println("[Bren Simple Debug] Animation test failed: " + e.getMessage());
                }
            }
        }
    }
}