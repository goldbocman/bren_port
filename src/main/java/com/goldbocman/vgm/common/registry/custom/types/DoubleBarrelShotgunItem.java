package com.goldbocman.vgm.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.registry.custom.PoseType;
import com.goldbocman.vgm.common.utils.GunHelper;

public class DoubleBarrelShotgunItem extends ShotgunItem {
    // 修改为接收Item.Settings参数的构造函数
    public DoubleBarrelShotgunItem(Item.Properties settings) {
        super(settings);
    }

    // 重写compatibleBullet方法，返回霰弹而不是普通子弹

    // 重写bulletAmount方法，返回1发子弹（每次射击发射1发，但可以连续发射两发）
    // 重写getMaxCapacity方法，将双管霰弹枪的容量设置为2
    @Override
    public int getMaxCapacity(ItemStack stack) {
        return 2; // 双管霰弹枪可以装2发子弹
    }

    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {

        // 关键修复：返回true表示应用了自定义矩阵变换
        // 返回false可能导致模型在动画结束时消失
        return super.applyCustomMatrix(entity, state, matrices, stack, cooldownProgress, leftHanded);
    }

    // 重写holdingPose方法，使用双臂持握
    @Override
    public PoseType holdingPose() {
        return PoseType.TWO_ARMS;
    }


}
