package com.goldbocman.vgm.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.ItemReg;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;

public abstract class BulletOnlyGun extends GunItem {

    private static final String BULLET_COUNT_KEY = "BulletCount";

    public BulletOnlyGun(Item.Properties settings) {
        super(settings);
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        // 为霰弹枪设置合适的容量，通常霰弹枪有6-8发容量
        // 这里设置为6发，可以根据实际需求调整
        return 6; // 修复：直接返回int值，而不是Optional.of(6)
    }

    @Override
    public int getContents(ItemStack stack) {
        // 修复：返回int类型而不是Optional<Integer>
        var nbtComponent = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);

        if (nbtComponent != null) {
            return GunApiCompat.getInt(nbtComponent.copyTag(), BULLET_COUNT_KEY);
        } else {
            return 0; // 默认值，直接返回int
        }
    }

    public void addContent(ItemStack stack) {
        int currentCount = getContents(stack); // 修复：直接使用int值，不需要.orElse(0)
        int newCount = Math.min(currentCount + 1, getMaxCapacity(stack)); // 修复：直接使用int值，不需要.orElse(0)

        var nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        nbt.putInt(BULLET_COUNT_KEY, newCount);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(nbt));
    }

    @Override
    public void useBullet(ItemStack stack) {
        int currentCount = getContents(stack);
        int newCount = Math.max(currentCount - 1, 0);

        var nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        nbt.putInt(BULLET_COUNT_KEY, newCount);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(nbt));
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return getContents(stack) <= 0;
    }

    @Override
    public void onReload(Player player) {
        ItemStack stack = player.getMainHandItem();
        ItemCooldowns cooldownManager = player.getCooldowns();

        if (player instanceof IGunUser gunUser && !GunApiCompat.isOnCooldown(cooldownManager, stack)) {
            ItemStack bullets = Bren.getItemFromPlayer(player, compatibleBullet(player));

            // 修改：允许在枪械未满且有弹药时继续装填，即使之前已经装填过
            if (bullets.isEmpty() || getContents(stack) >= getMaxCapacity(stack)) {
                return;
            }

            if (!gunUser.bren_1_21_1$canReload()) {
                return;
            }

            gunUser.bren_1_21_1$setCanReload(false);
            gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.RELOADING);
            gunUser.bren_1_21_1$setReloadingGun(stack);
            GunApiCompat.addCooldown(cooldownManager, stack, this.reloadSpeed());
            onInsert(stack, player);
        }
    }

    protected void onInsert(ItemStack stack, LivingEntity player) {
    }

    protected void afterInserted(ItemStack stack, LivingEntity player) {
    }

    protected void onFullyLoaded(ItemStack stack, LivingEntity player) {
    }

    public Item compatibleBullet(Player Player) {
        return ItemReg.BULLET;
    }

    @Override
    public void reloadTick(ItemStack stack, Level world, Player player, IGunUser gunUser) {
        ItemCooldowns cooldownManager = player.getCooldowns();

        if (!GunApiCompat.isOnCooldown(cooldownManager, stack) &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {

            Item compatibleBulletItem = this.compatibleBullet(player);
            ItemStack bullets = Bren.getItemFromPlayer(player, compatibleBulletItem);

            if (bullets.isEmpty() || getContents(stack) >= getMaxCapacity(stack)) {
                finishReload(gunUser, player);
                return;
            }

            addContent(stack);
            bullets.shrink(1);
            afterInserted(stack, player);
            finishReload(gunUser, player);
        }
    }

    private void finishReload(IGunUser gunUser, Player player) {
        gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
        gunUser.bren_1_21_1$setCanReload(true);
        gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
//        if (player instanceof ServerPlayer serverPlayer) {
//            NetworkReg.broadcastGunState(serverPlayer, false);
//        }
    }

    @Override
    protected Component getAmmoDescription() {
        return Component.literal("Uses: Bullet").withStyle(ChatFormatting.YELLOW);
    }

    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {
        return false;
    }

}