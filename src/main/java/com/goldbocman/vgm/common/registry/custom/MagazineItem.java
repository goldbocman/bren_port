package com.goldbocman.vgm.common.registry.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.utils.GunApiCompat;

public class MagazineItem extends Item {
    private final int capacity;

    public MagazineItem(Properties settings, int capacity) {
        super(settings);
        this.capacity = capacity;
    }
    //? if >=1.21.11 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipComponent, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag type) {
        ChatFormatting formatting = ChatFormatting.GRAY;

        tooltipAdder.accept(Component.translatable(String.format("desc.%s.item.magazine.content",Bren.MODID))
                .append(Component.literal(" " + getContents(stack) + "/" + getMaxCapacity(stack))).withStyle(formatting));

        super.appendHoverText(stack, context, tooltipComponent, tooltipAdder, type);
    }
    *///?} else {
    //? if >=1.20.5 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltipComponents, TooltipFlag type) {
        ChatFormatting formatting = ChatFormatting.GRAY;

        tooltipComponents.add(Component.translatable(String.format("desc.%s.item.magazine.content", Bren.MODID))
                .append(Component.literal(" " + getContents(stack) + "/" + getMaxCapacity(stack))).withStyle(formatting));

        super.appendHoverText(stack, context, tooltipComponents, type);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, java.util.List<Component> tooltipComponents, TooltipFlag type) {
        ChatFormatting formatting = ChatFormatting.GRAY;

        tooltipComponents.add(Component.translatable(String.format("desc.%s.item.magazine.content", Bren.MODID))
                .append(Component.literal(" " + getContents(stack) + "/" + getMaxCapacity(stack))).withStyle(formatting));

        super.appendHoverText(stack, level, tooltipComponents, type);
    }
    //?}
    //?}


    public static int getMaxCapacity(ItemStack stack) {
        // 从物品实例获取容量
        if (stack.getItem() instanceof MagazineItem magazineItem) {
            return magazineItem.capacity;
        }
        return 0; // 默认值
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (stack.getItem() instanceof MagazineItem) {
            return Math.round(getContents(stack) * 13.0F / (float) MagazineItem.getMaxCapacity(stack));
        }
        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Bren.UNIVERSAL_AMMO_COLOR;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getContents(stack) > 0;
    }

    public static int getContents(ItemStack itemStack) {
        CompoundTag nbt = GunApiCompat.getTag(itemStack);
        if (nbt.contains("Contents")) {
            return GunApiCompat.getInt(nbt, "Contents");
        }
        return 0;
    }

    public static boolean isEmpty(ItemStack stack) {
        return getContents(stack) <= 0;
    }

    public static boolean isFull(ItemStack mag) {
        if (mag.getItem() instanceof MagazineItem) {
            return MagazineItem.getContents(mag) >= MagazineItem.getMaxCapacity(mag);
        }
        return false;
    }

    public static int fillMagazine(ItemStack mag, int amount) {
        if (mag.getItem() instanceof MagazineItem) {
            int original = getContents(mag);
            int maxCapacity = getMaxCapacity(mag);
            int newContents = Math.min(original + amount, maxCapacity);


            // 使用数据组件系统设置内容
            var nbt = GunApiCompat.getTag(mag);
            nbt.putInt("Contents", newContents);
            GunApiCompat.setTag(mag, nbt);

            // Keep the HAS_AMMO component synchronized with the actual ammunition quantity, driving
            // the full/empty texture switching. HAS_AMMO doesn't exist before 1.20.5 and is only read
            // for display on >=1.21.11 anyway (<1.21.11 reads the same NBT above directly via
            // LegacyItemPropertiesAccessor - see claude/gun-rendering.md), so this is a real no-op on
            // 1.20.1, not a partial reimplementation.
            //? if >=1.20.5 {
            /*if (newContents > 0) {
                mag.set(com.goldbocman.vgm.common.registry.DataComponentReg.HAS_AMMO, true);
            } else {
                mag.remove(com.goldbocman.vgm.common.registry.DataComponentReg.HAS_AMMO);
            }
            *///?}

            // 返回实际填充的子弹数量
            return newContents - original;
        }
        return 0;
    }

    public int getCapacity() {
        return capacity;
    }
}