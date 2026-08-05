package com.goldbocman.vgm.common.entity;

import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.utils.GunHelper;

import java.util.function.Predicate;

public interface IGunUser {

    void bren_1_21_1$setReloadingGun(ItemStack reloadingGun);
    
    ItemStack bren_1_21_1$getReloadingGun();

    boolean bren_1_21_1$isShooting();

    boolean bren_1_21_1$canShoot(Predicate<ItemStack> predicate);

    void bren_1_21_1$setGunTicks(int t);

    int bren_1_21_1$shootingDuration();

    int bren_1_21_1$getGunTicks();

    void bren_1_21_1$setCanReload(boolean b);

    boolean bren_1_21_1$canReload();

    ItemStack bren_1_21_1$getLastGun();

    GunHelper.GunStates bren_1_21_1$getGunState();

    void bren_1_21_1$setGunState(GunHelper.GunStates state);

    // 新增瞄准相关方法
    boolean bren_1_21_1$isAiming();
    
    void bren_1_21_1$setAiming(boolean aiming);
    
    float bren_1_21_1$getAimProgress();
    
    void bren_1_21_1$setAimProgress(float progress);
}