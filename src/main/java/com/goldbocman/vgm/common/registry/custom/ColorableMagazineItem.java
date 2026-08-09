package com.goldbocman.vgm.common.registry.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.utils.GunApiCompat;

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
        var nbt = GunApiCompat.getTag(stack);

        // 获取或创建display标签
        CompoundTag displayNbt;
        if (nbt.contains("display")) {
            displayNbt = nbt;
        } else {
            displayNbt = new CompoundTag();
            nbt.put("display", displayNbt);
        }

        displayNbt.putInt("color", color);
        GunApiCompat.setTag(stack, nbt);
    }
}