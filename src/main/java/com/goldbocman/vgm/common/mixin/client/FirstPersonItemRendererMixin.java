package com.goldbocman.vgm.common.mixin.client;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.PoseStack;
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
//? if fabric
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
                boolean leftHanded = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

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

            applyFirstPersonAnimationLogic(poseStack, entity, stack, cooldownProgress, reloading, leftHanded);
        }
    }
    
    @Unique
    private void applyFirstPersonAnimationLogic(PoseStack poseStack, LivingEntity entity, ItemStack stack,
                                               float cooldownProgress, boolean reloading, boolean leftHanded) {
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem gunItem) {
            GunHelper.GunStates gunState = reloading ? GunHelper.GunStates.RELOADING : GunHelper.GunStates.NORMAL;
            gunItem.applyCustomMatrix(entity, gunState, poseStack, stack, cooldownProgress, leftHanded);
        }
    }
    
    @Unique
    private static boolean isFirstPersonRender(ItemDisplayContext itemDisplayContext) {
        // 通过ItemDisplayContext判断是否为第一人称渲染
        return itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
               itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }
}
//?}