package nl.sniffiandros.bren.common.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;
import nl.sniffiandros.bren.common.utils.GunHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
@Environment(value= EnvType.CLIENT)
@Mixin(SpecialModelWrapper.class)
public abstract class ItemRendererMixin {

    // Injection point for the new rendering system based on Minecraft 26.1-alpha

    // Injected into the SpecialModelWrapper.update method, which is a core method of the new rendering system
    @Inject(at = @At("HEAD"), method = "update")
    private void bren$renderGunAnimation(
            ItemStackRenderState output, 
            ItemStack item, 
            ItemModelResolver resolver, 
            ItemDisplayContext displayContext, 
            @Nullable net.minecraft.client.multiplayer.ClientLevel level, 
            @Nullable net.minecraft.world.entity.ItemOwner owner, 
            int seed, 
            CallbackInfo ci) {
        
        Minecraft client = Minecraft.getInstance();

        // Only process items owned by the local player to prevent player1's animation from bleeding onto player2
        if (client.player == null || owner != client.player) return;

        // Only processed when the client is active and the player exists
        if (client.getCameraEntity() instanceof LivingEntity livingEntity) {
            ItemStack mainHandItem = client.player.getMainHandItem();
            ItemStack offHandItem = client.player.getOffhandItem();

            // Check if it is a firearm (primary or secondary hand).
            boolean isMainHandGun = !mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem;
            boolean isOffHandGun = !offHandItem.isEmpty() && offHandItem.getItem() instanceof GunItem;

            // Debugging information: Displays the current rendering status
            if (isMainHandGun || isOffHandGun) {
                System.out.println("[Bren Debug] Gun animation triggered for display context: " + displayContext);
            }

            // Check if it is in first-person rendering mode
            boolean isFirstPerson = isFirstPersonRender(displayContext);

            // Debugging information: Displays the current rendering mode
            if (isMainHandGun || isOffHandGun) {
                System.out.println("[Bren Debug] Display context: " + displayContext + ", isFirstPerson: " + isFirstPerson);
            }
            
            if (isFirstPerson) {
                // Apply first-person gun animation transition to rendering state
                if (isMainHandGun) {
                    System.out.println("[Bren Debug] Applying first person animation to main hand");
                    applyFirstPersonAnimationTransform(output, livingEntity, mainHandItem);
                } else if (isOffHandGun) {
                    System.out.println("[Bren Debug] Applying first person animation to off hand");
                    applyFirstPersonAnimationTransform(output, livingEntity, offHandItem);
                }
            } else {
                // Apply third-person gun animation transition to rendering state
                if (isMainHandGun) {
                    applyThirdPersonAnimationTransform(output, livingEntity, mainHandItem);
                } else if (isOffHandGun) {
                    applyThirdPersonAnimationTransform(output, livingEntity, offHandItem);
                }
            }
        }
    }

    @Unique
    private void applyFirstPersonAnimationTransform(ItemStackRenderState output, LivingEntity livingEntity, ItemStack itemStack) {
        // First-person animation logic - applying transformations via ItemStackRenderState
        // Here you can add animation effects specific to first-person perspective.
        if (livingEntity instanceof Player player && livingEntity instanceof IGunUser gunUser) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof GunItem gunItem) {
                float cooldownProgress = player.getCooldowns().getCooldownPercent(itemStack, 0.0F);
                GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
                boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);
                boolean leftHanded = livingEntity.getMainArm().equals(HumanoidArm.LEFT);

                // Apply first-person animation logic
                applyFirstPersonAnimationLogic(output, livingEntity, itemStack, cooldownProgress, reloading, leftHanded);
            }
        }
    }
    
    @Unique
    private void applyThirdPersonAnimationTransform(ItemStackRenderState output, LivingEntity livingEntity, ItemStack itemStack) {
        // Third-person animation logic - applying transformations via ItemStackRenderState
        // Here you can add animation effects specific to third-person perspective.
        if (livingEntity instanceof Player player && livingEntity instanceof IGunUser gunUser) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof GunItem gunItem) {
                float cooldownProgress = player.getCooldowns().getCooldownPercent(itemStack, 0.0F);
                GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
                boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);
                boolean leftHanded = livingEntity.getMainArm().equals(HumanoidArm.LEFT);

                // Apply third-person animation logic
                applyThirdPersonAnimationLogic(output, livingEntity, itemStack, cooldownProgress, reloading, leftHanded);
            }
        }
    }
    
    @Unique
    private void applyFirstPersonAnimationLogic(ItemStackRenderState output, LivingEntity entity, ItemStack itemStack, float cooldownProgress, boolean reloading, boolean leftHanded) {
        // First-person animation logic - refined close-up animation effects
        // Get time parameters
        float f = cooldownProgress;
        float f1 = cooldownProgress;

        // If the item is a GunItem and implements the applyCustomMatrix method, then call the custom matrix transformation.
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof GunItem gunItem) {
            GunHelper.GunStates gunState = reloading ? GunHelper.GunStates.RELOADING : GunHelper.GunStates.NORMAL;

            // Note: In the new rendering system, we do not use PoseStack, but instead apply transformations through ItemStackRenderState
            // Therefore, applyCustomMatrix is no longer called here, as it expects a non-null PoseStack
            // All animation transformations are handled through the ItemStackRenderState API
        }


        // Use the correct 1.21.6 method to get tickDelta
        Minecraft client = Minecraft.getInstance();
        float delta = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        // First-person animation logic (based on version 1.21.6)
        float sin = (float) Math.sin((f * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
        float sin2 = (float) Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
        float sin3 = reloading ? sin2 : (float) Math.sin(1 - f);
        
        double d = (Math.sin(((float) entity.tickCount + delta) / 2) * (reloading ? sin2 : f1)) * 30;

        // In the new rendering system, transformations are applied by modifying the rendering state.
        // Here, we need to apply animation transformations based on the ItemStackRenderState API.
        // This is a placeholder for now; implementation will follow based on the specific API.
        applyFirstPersonTransformViaRenderState(output, sin, sin2, sin3, d, leftHanded, reloading);
    }
    
    @Unique
    private void applyThirdPersonAnimationLogic(ItemStackRenderState output, LivingEntity entity, ItemStack itemStack, float cooldownProgress, boolean reloading, boolean leftHanded) {
        // Third-person animation logic - Simplified long-distance animation effects
        // Get time parameters
        float f = cooldownProgress;
        float f1 = cooldownProgress;

        // If the item is a GunItem and implements the applyCustomMatrix method, then call the custom matrix transformation.
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof GunItem gunItem) {
            GunHelper.GunStates gunState = reloading ? GunHelper.GunStates.RELOADING : GunHelper.GunStates.NORMAL;

            // Note: In the new rendering system, we do not use PoseStack, but instead apply transformations through ItemStackRenderState
            // Therefore, applyCustomMatrix is ​​no longer called here, as it expects a non-null PoseStack
            // All animation transformations are handled through the ItemStackRenderState API
        }

        // Third-person animation logic (based on version 1.21.6)
        float z = Math.max((1 - f + f1) / 2, 0);
        float f2 = reloading ? ((float) Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5F + 0.5F) / 3 : z;

        // In the new rendering system, transformations are applied by modifying the rendering state.
        // Here, we need to apply animation transformations based on the ItemStackRenderState API.
        // This is a placeholder for now; implementation will follow based on the specific API.
        applyThirdPersonTransformViaRenderState(output, f2, leftHanded);
    }
    
    @Unique
    private void applyFirstPersonTransformViaRenderState(ItemStackRenderState output, float sin, float sin2, float sin3, double d, boolean leftHanded, boolean reloading) {
        // 通过 ItemStackRenderState API 应用第一人称动画变换
        try {
            // 在 Minecraft 26.1 中，ItemStackRenderState 使用新的API
            // 尝试使用新的方法来应用变换
            
            // 方法1：尝试使用新的变换API（基于Minecraft 26.1的渲染系统）
            try {
                // 使用新的变换方法，避免使用反射
                applyModernFirstPersonTransform(output, sin, sin2, sin3, d, leftHanded, reloading);
                System.out.println("[Bren Debug] First person animation applied successfully via modern API");
                return; // 如果成功，直接返回
            } catch (Exception e) {
                System.err.println("[Bren Debug] Modern API failed: " + e.getMessage());
            }
            
            // 方法2：尝试使用基础的位置和旋转设置
            try {
                // 使用更安全的方法来设置位置和旋转
                applySafeFirstPersonTransform(output, sin, sin2, sin3, d, leftHanded, reloading);
                System.out.println("[Bren Debug] First person animation applied successfully via safe method");
                return; // 如果成功，直接返回
            } catch (Exception e) {
                System.err.println("[Bren Debug] Safe method failed: " + e.getMessage());
            }
            
            // 如果所有方法都失败，记录调试信息
            System.err.println("[Bren Debug] All first person animation methods failed");
            
        } catch (Exception e) {
            // 如果所有方法都失败，记录错误但不中断游戏
            System.err.println("[Bren Debug] Failed to apply first person animation transform: " + e.getMessage());
        }
    }
    
    @Unique
    private void applyModernFirstPersonTransform(ItemStackRenderState output, float sin, float sin2, float sin3, double d, boolean leftHanded, boolean reloading) {
        // 基于 Minecraft 26.1 新渲染系统的变换方法
        // 使用正确的API来应用动画变换

        // 计算动画参数
        float zOffset = reloading ? 0 : (sin / 2 + sin2 / 4) * 0.5F;
        float zRotation = (float) (leftHanded ? -15 + d : 15 + d);
        float xRotation = (sin3 * 10) * 0.5F;
        
        // 新增：向前且向外侧偏移
        float forwardOffset = 0.1F; // 向前偏移量
        float sideOffset = leftHanded ? -0.05F : 0.05F; // 向外侧偏移（左手向左，右手向右）
        
        // 尝试使用正确的变换方法
        try {
            // 使用反射访问layers字段（复数形式）
            java.lang.reflect.Field layersField = output.getClass().getDeclaredField("layers");
            layersField.setAccessible(true);
            Object[] layers = (Object[]) layersField.get(output);
            
            if (layers != null && layers.length > 0) {
                // 获取第一个LayerRenderState
                Object firstLayer = layers[0];
                
                // 获取localTransform字段
                java.lang.reflect.Field localTransformField = firstLayer.getClass().getDeclaredField("localTransform");
                localTransformField.setAccessible(true);
                org.joml.Matrix4f localTransform = (org.joml.Matrix4f) localTransformField.get(firstLayer);
                
                if (localTransform != null) {
                    // 应用变换到localTransform矩阵（包含向前和向外侧偏移）
                    localTransform.translate(sideOffset, 0, zOffset + forwardOffset);
                    localTransform.rotateZ((float) Math.toRadians(zRotation));
                    localTransform.rotateX((float) Math.toRadians(xRotation));
                    
                    System.out.println("[Bren Debug] First person animation applied via localTransform with forward/side offset");
                    return;
                }
            }
            
            throw new RuntimeException("Layers or localTransform not found");
            
        } catch (Exception e) {
            // 如果反射方法失败，回退到其他方法
            throw new RuntimeException("Modern API failed: " + e.getMessage());
        }
    }
    
    @Unique
    private void applySafeFirstPersonTransform(ItemStackRenderState output, float sin, float sin2, float sin3, double d, boolean leftHanded, boolean reloading) {
        // 安全的第一人称动画变换方法
        // 使用更保守的动画参数，避免过度变换
        
        float simpleOffset = reloading ? 0.02F : 0.05F; // 进一步减少偏移量
        float simpleZRotation = leftHanded ? -3 : 3; // 进一步减少旋转角度
        float simpleXRotation = reloading ? 1 : 2; // 进一步减少X轴旋转
        
        // 应用简单的动画变换
        try {
            // 使用反射访问layers字段（复数形式）
            java.lang.reflect.Field layersField = output.getClass().getDeclaredField("layers");
            layersField.setAccessible(true);
            Object[] layers = (Object[]) layersField.get(output);
            
            if (layers != null && layers.length > 0) {
                // 获取第一个LayerRenderState
                Object firstLayer = layers[0];
                
                // 获取localTransform字段
                java.lang.reflect.Field localTransformField = firstLayer.getClass().getDeclaredField("localTransform");
                localTransformField.setAccessible(true);
                org.joml.Matrix4f localTransform = (org.joml.Matrix4f) localTransformField.get(firstLayer);
                
                if (localTransform != null) {
                    // 应用简单的变换
                    localTransform.translate(0, 0, simpleOffset);
                    localTransform.rotateZ((float) Math.toRadians(simpleZRotation));
                    localTransform.rotateX((float) Math.toRadians(simpleXRotation));
                    
                    System.out.println("[Bren Debug] Safe first person animation applied via localTransform");
                    return;
                }
            }
            
            throw new RuntimeException("Layers or localTransform not found");
            
        } catch (Exception e) {
            // 如果反射方法失败，抛出异常
            throw new RuntimeException("Safe method failed: " + e.getMessage());
        }
    }
    
    @Unique
    private void applyThirdPersonTransformViaRenderState(ItemStackRenderState output, float f2, boolean leftHanded) {
        // 通过 ItemStackRenderState API 应用第三人称动画变换
        try {
            // 方法1：尝试使用LayerRenderState的localTransform
            try {
                // 使用反射访问layers字段（复数形式）
                java.lang.reflect.Field layersField = output.getClass().getDeclaredField("layers");
                layersField.setAccessible(true);
                Object[] layers = (Object[]) layersField.get(output);
                
                if (layers != null && layers.length > 0) {
                    // 获取第一个LayerRenderState
                    Object firstLayer = layers[0];
                    
                    // 获取localTransform字段
                    java.lang.reflect.Field localTransformField = firstLayer.getClass().getDeclaredField("localTransform");
                    localTransformField.setAccessible(true);
                    org.joml.Matrix4f localTransform = (org.joml.Matrix4f) localTransformField.get(firstLayer);
                    
                    if (localTransform != null) {
                        // 应用第三人称动画变换
                        float yRotation = leftHanded ? 10 : -10;
                        float xRotation = f2 * 30 + 45;
                        
                        localTransform.rotateY((float) Math.toRadians(yRotation));
                        localTransform.rotateX((float) Math.toRadians(xRotation));
                        localTransform.translate(0, -f2 / 4 + 0.25F, f2 / 8 - 0.25F);
                        
                        System.out.println("[Bren Debug] Third person animation applied successfully via localTransform");
                        return; // 如果成功，直接返回
                    }
                }
                
                throw new RuntimeException("Layers or localTransform not found");
                
            } catch (Exception e) {
                System.err.println("[Bren Debug] Third person localTransform method failed: " + e.getMessage());
            }
            
            // 方法2：尝试使用更简单的动画效果
            try {
                // 使用反射访问layers字段（复数形式）
                java.lang.reflect.Field layersField = output.getClass().getDeclaredField("layers");
                layersField.setAccessible(true);
                Object[] layers = (Object[]) layersField.get(output);
                
                if (layers != null && layers.length > 0) {
                    // 获取第一个LayerRenderState
                    Object firstLayer = layers[0];
                    
                    // 获取localTransform字段
                    java.lang.reflect.Field localTransformField = firstLayer.getClass().getDeclaredField("localTransform");
                    localTransformField.setAccessible(true);
                    org.joml.Matrix4f localTransform = (org.joml.Matrix4f) localTransformField.get(firstLayer);
                    
                    if (localTransform != null) {
                        // 应用简单的动画变换
                        float simpleRotation = leftHanded ? 15 : -15;
                        localTransform.rotateY((float) Math.toRadians(simpleRotation));
                        localTransform.translate(0, 0.1F, 0.1F);
                        
                        System.out.println("[Bren Debug] Simple third person animation applied successfully");
                        return;
                    }
                }
                
                throw new RuntimeException("Layers or localTransform not found");
                
            } catch (Exception e) {
                System.err.println("[Bren Debug] Simple third person animation approach also failed: " + e.getMessage());
            }
            
            // 如果所有方法都失败，记录调试信息
            System.err.println("[Bren Debug] All third person animation methods failed");
            
        } catch (Exception e) {
            // 如果所有方法都失败，记录错误但不中断游戏
            System.err.println("[Bren Debug] Failed to apply third person animation transform: " + e.getMessage());
        }
    }

    @Unique
    private static boolean isFirstPersonRender(net.minecraft.world.item.ItemDisplayContext itemDisplayContext) {
        // 通过ItemDisplayContext判断是否为第一人称渲染
        
        // 调试：输出所有显示上下文信息
        System.out.println("[Bren Debug] Checking display context: " + itemDisplayContext + ", name: " + itemDisplayContext.name());
        
        // 检查所有可能的第一人称显示上下文
        boolean isFirstPerson = itemDisplayContext == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND || 
                               itemDisplayContext == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        
        System.out.println("[Bren Debug] isFirstPerson result: " + isFirstPerson);
        
        return isFirstPerson;
    }

    @Unique
    private static void applyGunAnimationTransform(PoseStack matrices, LivingEntity entity, ItemStack itemStack) {
        // 这里可以应用后坐力、装弹等动画效果

        // 示例：根据枪械状态应用不同的动画变换
        if (entity instanceof Player player && entity instanceof IGunUser gunUser) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof GunItem gunItem) {
                // 获取枪械状态信息
                float cooldownProgress = player.getCooldowns().getCooldownPercent(itemStack, 0.0F);
                GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
                boolean reloading = gunState.equals(GunHelper.GunStates.RELOADING);
                boolean leftHanded = entity.getMainArm().equals(HumanoidArm.LEFT);

                // 应用1.21.6版本的动画逻辑
                apply126AnimationLogic(matrices, entity, itemStack, cooldownProgress, reloading, leftHanded);

            }
        }
    }


    @Unique
    private static void apply126AnimationLogic(PoseStack matrices, LivingEntity entity, ItemStack itemStack, float cooldownProgress, boolean reloading, boolean leftHanded) {
        // 获取时间参数
        float f = cooldownProgress;
        float f1 = cooldownProgress;

        // 如果物品是GunItem且实现了applyCustomMatrix方法，则调用自定义矩阵变换
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof GunItem gunItem) {
            GunHelper.GunStates gunState = reloading ? GunHelper.GunStates.RELOADING : GunHelper.GunStates.NORMAL;

            // 应用自定义矩阵变换
            gunItem.applyCustomMatrix(entity, gunState, matrices, itemStack, cooldownProgress, leftHanded);
        }

        // 使用正确的1.21.6方法获取tickDelta
        Minecraft client = Minecraft.getInstance();
        float delta = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        Identifier itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());

        // 判断是否为第一人称渲染
        boolean isFirstPerson = client.options.getCameraType().isFirstPerson();
        
        if (isFirstPerson) {
            // 第一人称动画逻辑（基于1.21.6版本）
            float sin = (float) Math.sin((f * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
            float sin2 = (float) Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
            float sin3 = reloading ? sin2 : (float) Math.sin(1 - f);
            
            double d = (Math.sin(((float) entity.tickCount + delta) / 2) * (reloading ? sin2 : f1)) * 30;
            
            // 调整Z轴位置，减少模型过于靠前的问题
            float zOffset = reloading ? 0 : (sin / 2 + f1 / 4) * 0.5F; // 减少50%的偏移量
            matrices.translate(0, 0, zOffset);
            matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) (leftHanded ? -15 + d : 15 + d)));
            matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees((sin3 * 10) * 0.5F)); // 降低后坐力强度
            
        } else {
            // 第三人称动画逻辑（基于1.21.6版本）
            float z = Math.max((1 - f + f1) / 2, 0);
            float f2 = reloading ? ((float) Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5F + 0.5F) / 3 : z;
            matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(leftHanded ? 10 : -10));
            matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(f2 * 30 + 45));
            
            matrices.translate(0, -f2 / 4 + 0.25F, f2 / 8 - 0.25F);
        }
    }

}