package com.goldbocman.vgm.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.ItemReg;
import com.goldbocman.vgm.common.registry.SoundReg;
import com.goldbocman.vgm.common.registry.custom.PoseType;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;
import org.spongepowered.asm.mixin.Unique;

public class RevolverItem extends BulletOnlyGun {
    // 修改构造函数，接收Item.Settings参数
    public RevolverItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public int reloadSpeed() {
        return 6;
    }

    @Override
    public PoseType holdingPose() {
        return PoseType.REVOLVER;
    }

    @Override
    public Item compatibleBullet(Player player) {
        return ItemReg.MAGNUM_BULLET;
    }

    @Override
    protected Component getAmmoDescription() {
        return Component.literal("Uses: Magnum Bullet").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public int ammoIconOffset() {
        return 24;
    }

    @Override
    protected void afterInserted(ItemStack stack, LivingEntity player) {
        player.level().playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundReg.ITEM_REVOLVER_RELOAD,
                SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
    }

    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {
        // 关键修复：添加null检查，避免matrices为null时崩溃
        if (matrices == null) {
            return false;
        }
        
        if (entity instanceof IGunUser gunUser && cooldownProgress > 0) {
            try {
                Minecraft client = Minecraft.getInstance();
                boolean isFirstPerson = client.options.getCameraType().isFirstPerson();
                boolean reloading = gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING);

                float f = cooldownProgress;
                float f1 = cooldownProgress;

                float sin = (float) Math.sin((f * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
                float sin2 = (float) Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;
                float sin3 = reloading ? sin2 : (float) Math.sin(1 - f);

                float delta = GunApiCompat.getPartialTick(client);

                double d = (Math.sin(((float) entity.tickCount + delta) / 6) * (reloading ? sin2 : f1)) * 2;
                // 添加额外的平滑处理：使用缓动函数来减少动画的突变
                // 在开始和结束时使用缓入缓出效果
                float easedSin = easeInOutCubic(sin);

                // 第一人称视角下的位置偏移：将左轮手枪调整到更自然的持枪位置
                if (isFirstPerson) {
                    // 使用平滑的缓动值，减少卡顿感
                    float zOffset = reloading ? 0 : (easedSin / 3 + f1 / 6) * 0.3F;
                    matrices.translate(0, 0, zOffset);
                }

                // 装弹动画：在Y轴方向上应用平滑的正弦波动画，模拟装弹时的上下移动
                // 使用缓动后的值，使移动更加自然
                matrices.translate(0, (reloading ? easedSin / 3 : 0), 0);
                // 应用Z轴旋转：根据左右手和装弹状态调整手枪角度
                // 左手：90+25度基础角度，装弹时额外增加sin*180度旋转
                // 右手：65度基础角度，装弹时额外增加sin*180度旋转
                matrices.mulPose(Axis.ZN.rotationDegrees((float) d * sin * 10));

                // 应用X轴旋转：基于冷却进度的轻微俯仰动画
                matrices.mulPose(Axis.XN.rotation((float) (d * 8)));
                
                return true;
            } catch (Exception e) {
                // 如果动画应用失败，记录错误但不中断游戏
                System.err.println("[Bren Debug] Failed to apply revolver animation: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    // 缓动函数：提供平滑的动画过渡效果
    @Unique
    private static float easeInOutCubic(float x) {
        // 缓入缓出三次方函数，在开始和结束时平滑过渡
        return x < 0.5 ? 4 * x * x * x : 1 - (float)Math.pow(-2 * x + 2, 3) / 2;
    }

    // 移除旧的构造函数，保留其他方法
}