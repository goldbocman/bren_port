package com.goldbocman.vgm.common.registry.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ColorableMagazineItem extends MagazineItem {
    public ColorableMagazineItem(Properties settings, int capacity) {
        super(settings, capacity);
    }

//    public int getColor(ItemStack stack) {
//        // 使用标准的NBT方法获取颜色
//        NbtComponent contents = stack.get(DataComponentTypes.CUSTOM_DATA);
//        if (contents != null) {
//            NbtCompound nbt = contents.copyNbt();
//            if (nbt != null && nbt.contains("display")) {
//                NbtCompound displayNbt = nbt.getCompound("display");
//                if (displayNbt.contains("color")) { // 99 表示NUMBER_TYPE
//                    return displayNbt.getInt("color");
//                }
//            }
//        }
//        return 0x6A4C40; // 默认棕色
//    }

    public static void setColor(ItemStack stack, int color) {
        // 使用数据组件系统设置颜色
        var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, 
            CustomData.EMPTY).copyTag();
        
        // 获取或创建display标签
        CompoundTag displayNbt;
        if (nbt.contains("display")) {
            displayNbt = nbt;
        } else {
            displayNbt = new CompoundTag();
            nbt.put("display", displayNbt);
        }
        
        displayNbt.putInt("color", color);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
}