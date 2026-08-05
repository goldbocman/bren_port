package com.goldbocman.vgm.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.registry.custom.PoseType;
import com.goldbocman.vgm.common.utils.GunHelper;

public class LeverGunItem extends RevolverItem {
    public LeverGunItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        // 为霰弹枪设置合适的容量，通常霰弹枪有6-8发容量
        // 这里设置为6发，可以根据实际需求调整
        return 16; // 修复：直接返回int值，而不是Optional.of(6)
    }

    @Override
    public PoseType holdingPose() {
        return PoseType.TWO_ARMS;
    }


    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {
        // 关键修复：添加null检查，避免matrices为null时崩溃
        if (matrices == null) {
            return false;
        }

        try {
            Minecraft client = Minecraft.getInstance();
            boolean isFirstPerson = client.options.getCameraType().isFirstPerson();

            if (state == GunHelper.GunStates.NORMAL && isFirstPerson) {
                // 修复动画计算：确保在动画结束时模型不会消失
                // 使用更安全的动画计算，避免负值和异常

                // 确保cooldownProgress在有效范围内
                float progress = Math.max(0, Math.min(cooldownProgress, 1.0F));

                // 改进的动画计算：使用平滑的缓动函数
                float f = Math.max(0, progress - 0.1F) * 2;  // 从0.5开始动画
                float f1 = Math.max(0, progress - 0.2F) * 3; // 从0.333开始动画

                // 使用平滑的正弦波，避免绝对值导致的突变
                float sin1 = (float) Math.sin(f * Math.PI);
                float sin2 = (float) Math.sin(f1 * Math.PI);

                // 确保动画值在合理范围内
                sin1 = Math.max(0, sin1);
                sin2 = Math.max(0, sin2);

                // 应用平滑的动画变换
                matrices.translate(0, sin1 * 0.3F - sin2 * 0.7F, -0.2F * sin1);
                matrices.mulPose(Axis.XP.rotation(sin1 * 1.047198F));
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            // 如果动画应用失败，记录错误但不中断游戏
            System.err.println("[Bren Debug] Failed to apply lever gun animation: " + e.getMessage());
            return false;
        }
    }


}