package nl.sniffiandros.bren.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.entity.BulletEntity;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.ItemReg;
import nl.sniffiandros.bren.common.registry.SoundReg;
import nl.sniffiandros.bren.common.utils.GunHelper;

public class ShotgunItem extends BulletOnlyGun {
    private Player Player
            ;

    // 修改为接收Item.Settings参数的构造函数
    public ShotgunItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    protected Component getAmmoDescription() {
        return Component.literal("Uses: Shell").withStyle(ChatFormatting.YELLOW);
    }

    // 获取当前使用的子弹类型
    public int getCurrentBulletType(ItemStack stack) {
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        return customData.copyTag().getInt("CurrentBulletType").orElse(BulletEntity.TYPE_NORMAL);
    }

    // 设置当前使用的子弹类型
    public void setCurrentBulletType(ItemStack stack, int bulletType) {
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        var updatedTag = customData.copyTag();
        updatedTag.putInt("CurrentBulletType", bulletType);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(updatedTag));
    }

    @Override
    protected void onInsert(ItemStack stack, LivingEntity player) {

        Level world = player.level();

        world.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundReg.ITEM_SHOTGUN_SHELL_INSERT,
                SoundSource.PLAYERS, 1.0F, 1.0F);

    }

    @Override
    protected void onFullyLoaded(ItemStack stack, LivingEntity player) {

        Level world = player.level();

        world.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundReg.ITEM_SHOTGUN_RACK,
                SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
    }

    // 重写compatibleBullet方法，支持多种子弹类型


    // 重写方法以支持多种兼容的子弹

    @Override
    public int reloadSpeed() {
        return 8;
    }


    // 重写reloadTick方法，支持多种子弹类型的装填
    @Override
    public void reloadTick(ItemStack stack, Level world, Player player, IGunUser gunUser) {
        ItemCooldowns cooldownManager = player.getCooldowns();
        float cooldownProgress = cooldownManager.getCooldownPercent(stack, 1.0F);

        // 关键修复：确保只有在装弹状态下才执行装弹逻辑
        if (!cooldownManager.isOnCooldown(stack) &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {

            // 检查玩家是否有兼容的弹药（SHELL 或 DRAGONBREATH_SHELL）
            ItemStack shellBullets = Bren.getItemFromPlayer(player, ItemReg.SHELL);
            ItemStack dragonBreathBullets = ItemReg.DRAGONBREATH_SHELL != null
                    ? Bren.getItemFromPlayer(player, ItemReg.DRAGONBREATH_SHELL)
                    : ItemStack.EMPTY;

            // 优先使用龙息弹，如果没有则使用普通霰弹
            ItemStack bullets = dragonBreathBullets.isEmpty() ? shellBullets : dragonBreathBullets;

            if (bullets.isEmpty()) {
                // 重置状态
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
                gunUser.bren_1_21_1$setCanReload(true);
                gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
                return;
            }

            // 检查枪械是否已满
            if (getContents(stack) >= getMaxCapacity(stack)) {
                // 重置状态
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
                gunUser.bren_1_21_1$setCanReload(true);
                gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
                return;
            }

            // 执行装弹逻辑
            addContent(stack);
            bullets.shrink(1);
            afterInserted(stack, player);

            // 关键修复：记录当前使用的子弹类型
            setCurrentBulletType(stack, bullets.getItem() == ItemReg.DRAGONBREATH_SHELL ?
                    BulletEntity.TYPE_DRAGONBREATH : BulletEntity.TYPE_NORMAL);

            // 重置状态
            gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
            gunUser.bren_1_21_1$setCanReload(true);
            gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
        }
    }


    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {

        // 关键修复：添加null检查，避免matrices为null时崩溃
        if (matrices == null) {
            return false;
        }

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
        }

        // 关键修复：返回true表示应用了自定义矩阵变换
        // 返回false可能导致模型在动画结束时消失
        return true;
    }


    @Override
    public Item compatibleBullet(Player Player) {
        ItemStack shellBullets = Bren.getItemFromPlayer(Player, ItemReg.SHELL);
        ItemStack dragonBreathBullets = ItemReg.DRAGONBREATH_SHELL != null
                ? Bren.getItemFromPlayer(Player, ItemReg.DRAGONBREATH_SHELL)
                : ItemStack.EMPTY;

        if (!dragonBreathBullets.isEmpty()) {
            return ItemReg.DRAGONBREATH_SHELL;
        } else if (!shellBullets.isEmpty()) {
            return ItemReg.SHELL;
        }
        return ItemReg.SHELL;
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        // 为霰弹枪设置合适的容量，通常霰弹枪有6-8发容量
        // 这里设置为8发，可以根据实际需求调整
        return 8; // 修复：直接返回int值，而不是Optional.of(8)
    }

    @Override
    public int bulletLifespan() {
        return 9;
    }

    @Override
    public float spread() {
        return 5.0F;
    }

    @Override
    public int bulletAmount() {
        return 5;
    }

    public void setPlayer(Player player) {
        Player = player;
    }

    // 移除旧的构造函数，保留其他方法
}