package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineGunItem extends GunWithMagItem {
    // 添加日志记录器
    private static final Logger LOGGER = LoggerFactory.getLogger(MachineGunItem.class);
    
    // 修改为接收Item.Settings参数的构造函数，调用父类的构造函数
    public MachineGunItem(Properties settings) {
        super(settings);
        LOGGER.info("Creating new MachineGunItem instance");
    }

    // 可选：提供自定义弹匣标签的构造函数
    public MachineGunItem(Properties settings, TagKey<Item> compatibleMagazines) {
        super(settings, compatibleMagazines);
        LOGGER.info("Creating new MachineGunItem instance with custom magazines");
    }

    @Override
    public int reloadSpeed() {
        // 机枪的装弹速度可以比普通枪械慢一些
        return 10; // 10（1.25秒）
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        // 机枪的弹匣容量通常较大
        return 120; // 修复：直接返回int值，而不是Optional.of(50)
    }
}