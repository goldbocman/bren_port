package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.SoundReg;
import com.goldbocman.vgm.common.utils.GunHelper;

public class AirGunItem extends BulletOnlyGun {

    public AirGunItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        // 为霰弹枪设置合适的容量，通常霰弹枪有6-8发容量
        // 这里设置为6发，可以根据实际需求调整
        return 5; // 修复：直接返回int值，而不是Optional.of(6)
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

    @Override
    public Item compatibleBullet(Player Player) {
        return Items.IRON_NUGGET;
    }

    @Override
    public int reloadSpeed() {
        return 7;
    }

    @Override
    public void reloadTick(ItemStack stack, Level world, Player player, IGunUser gunUser) {
        ItemCooldowns cooldownManager = player.getCooldowns();
        float cooldownProgress = cooldownManager.getCooldownPercent(stack, 1.0F);


        // 关键修复：确保只有在装弹状态下才执行装弹逻辑
        if (!cooldownManager.isOnCooldown(stack) &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {

            // 关键修复：使用当前枪械实例的compatibleBullet方法，确保调用子类重写的方法
            Item compatibleBulletItem = this.compatibleBullet(player);

            // 检查玩家是否有弹药
            ItemStack bullets = Bren.getItemFromPlayer(player, compatibleBulletItem);
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

            // 关键修改：每次只装填一发，然后重置状态，等待玩家再次按下R键
            gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
            gunUser.bren_1_21_1$setCanReload(true);
            gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);

            // 移除自动连续装填的逻辑
            // 不再设置新的冷却时间，让玩家可以立即进行下一次装填
        }
    }

}
