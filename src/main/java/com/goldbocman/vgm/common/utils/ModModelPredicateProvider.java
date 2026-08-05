package com.goldbocman.vgm.common.utils;

import net.minecraft.world.item.Item;
import com.goldbocman.vgm.common.registry.ItemReg;

public class ModModelPredicateProvider {
    public static void regModels() {
        regGun(ItemReg.MACHINE_GUN, true);
        regGun(ItemReg.AUTO_GUN, true);
        regGun(ItemReg.RIFLE, false);
        regGun(ItemReg.NETHERITE_MACHINE_GUN, true);
        regGun(ItemReg.NETHERITE_AUTO_GUN, true);
        regGun(ItemReg.NETHERITE_RIFLE, false);
        // 添加对左轮手枪的谓词注册
        regGun(ItemReg.REVOLVER, false);
        regGun(ItemReg.NETHERITE_REVOLVER, false);
        regMag(ItemReg.MAGAZINE);
        regMag(ItemReg.CLOTHED_MAGAZINE);
        regMag(ItemReg.SHORT_MAGAZINE);
    }


    private static void regGun(Item machineGun, boolean colorable) {
        // 在Minecraft 1.21.4中，模型谓词已通过JSON文件定义，不再需要代码注册
        // 保留空方法以避免编译错误
    }

    private static void regMag(Item magazine) {
        // 在Minecraft 1.21.4中，模型谓词已通过JSON文件定义，不再需要代码注册
        // 保留空方法以避免编译错误
    }
}