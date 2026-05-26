package nl.sniffiandros.bren.common.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;
import nl.sniffiandros.bren.common.utils.GunHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
@Environment(value= EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonItemRendererMixin {

    /**
     * 注入到第一人称物品渲染方法
     * 这是Minecraft 26.1中处理第一人称物品渲染的主要方法
     */
    @Inject(at = @At("HEAD"), method = "renderItem")
    private void bren$renderFirstPersonGunAnimation(
            LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int light, CallbackInfo ci) {
        
        Minecraft client = Minecraft.getInstance();
        
        // 只在客户端且玩家存在时处理
        if (client.player != null && entity instanceof Player player && entity instanceof IGunUser gunUser) {
            
            // 检查是否为枪械物品
            if (!stack.isEmpty() && stack.getItem() instanceof GunItem gunItem) {
                
                // 检查是否为第一人称渲染
                boolean isFirstPerson = isFirstPersonRender(displayContext);
                
                // 根据显示上下文判断是否为左手
                boolean leftHanded = displayContext == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                
                // 调试信息
                System.out.println("[Bren Debug] First person render triggered: " + displayContext + ", isFirstPerson: " + isFirstPerson + ", leftHanded: " + leftHanded);
                
                if (isFirstPerson) {
                    // 应用第一人称枪械动画
                    applyFirstPersonGunAnimation(poseStack, entity, stack, leftHanded);
                }
            }
        }
    }
    
    @Unique
    private void applyFirstPersonGunAnimation(PoseStack poseStack, LivingEntity entity, ItemStack stack, boolean leftHanded) {
        if (entity instanceof Player player && entity instanceof IGunUser gunUser) {
            
            // 获取枪械状态信息
            float cooldownProgress = player.getCooldowns().getCooldownPercent(stack, 0.0F);
            GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
            boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);
            
            // 调试信息
            System.out.println("[Bren Debug] Applying first person gun animation: cooldown=" + cooldownProgress + ", reloading=" + reloading);
            
            // 应用第一人称动画逻辑
            applyFirstPersonAnimationLogic(poseStack, entity, stack, cooldownProgress, reloading, leftHanded);
        }
    }
    
    @Unique
    private void applyFirstPersonAnimationLogic(PoseStack poseStack, LivingEntity entity, ItemStack stack, 
                                               float cooldownProgress, boolean reloading, boolean leftHanded) {
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem gunItem) {
            GunHelper.GunStates gunState = reloading ? GunHelper.GunStates.RELOADING : GunHelper.GunStates.NORMAL;

            // 应用自定义矩阵变换
            gunItem.applyCustomMatrix(entity, gunState, poseStack, stack, cooldownProgress, leftHanded);
        }
        // 获取时间参数
        float f = cooldownProgress;
        float f1 = cooldownProgress;
        
        // 使用正确的1.21.6方法获取tickDelta
        Minecraft client = Minecraft.getInstance();
        float delta = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        
        // 第一人称动画逻辑（基于1.21.6版本）
        float sin = (float) Math.sin((f * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
        float sin2 = (float) Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
        float sin3 = reloading ? sin2 : (float) Math.sin(1 - f);
        
        double d = (Math.sin(((float) entity.tickCount + delta) / 2) * (reloading ? sin2 : f1)) * 30;
        
        // 应用动画变换到PoseStack
        float zOffset = reloading ? 0 : (sin / 2 + sin2 / 4) * 0.5F;
        float yOffset = -0.1F; // 向下偏移，降低模型位置
        float zRotation = (float) (leftHanded ? -15 + d : 15 + d);
        float xRotation = (sin3 * 10) * -0.5F;
        
        // 新增：向前且向外侧偏移
        float forwardOffset = -0.4F; // 向前偏移量
        float sideOffset = leftHanded ? 0.05F : -0.05F; // 向外侧偏移（左手向左，右手向右）
        
        poseStack.translate(sideOffset, yOffset, zOffset + forwardOffset);
//     poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(zRotation));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRotation));
        
        System.out.println("[Bren Debug] First person animation applied successfully");
    }
    
    @Unique
    private static boolean isFirstPersonRender(net.minecraft.world.item.ItemDisplayContext itemDisplayContext) {
        // 通过ItemDisplayContext判断是否为第一人称渲染
        return itemDisplayContext == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND || 
               itemDisplayContext == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }
}