package com.goldbocman.vgm.common.registry;

//? if >=1.20.5 {
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;
//?}

// DataComponentType doesn't exist before 1.20.5. HAS_MAGAZINE/HAS_AMMO/GUN_MODEL_TYPE are display-
// only conveniences already bypassed on <1.21.11 via ClientBren's LegacyItemPropertiesAccessor-
// registered ItemProperties predicates (which re-derive the same display state straight from
// CustomData/NBT instead - see claude/gun-rendering.md), so register() is a genuine no-op below
// 1.20.5, not a partial reimplementation.
public class DataComponentReg {
    //? if <1.20.5
    //public static void register() {}

    //? if >=1.20.5 {
    // 声明数据组件类型（先不初始化）
    public static DataComponentType<Boolean> HAS_MAGAZINE;
    public static DataComponentType<Integer> GUN_MODEL_TYPE;
    public static DataComponentType<Boolean> HAS_AMMO;

    /**
     * 注册所有数据组件类型
     */
    public static void register() {
        // 注册 HAS_MAGAZINE 组件
        HAS_MAGAZINE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceKey.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "has_magazine")),
            DataComponentType.<Boolean>builder()
                .persistent(com.mojang.serialization.Codec.BOOL)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                .build()
        );

        // Register the HAS_AMMO component - used for toggling the full/empty texture of the magazine item itself.
        HAS_AMMO = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceKey.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "has_ammo")),
            DataComponentType.<Boolean>builder()
                .persistent(com.mojang.serialization.Codec.BOOL)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                .build()
        );

        // 注册 GUN_MODEL_TYPE 组件
        GUN_MODEL_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceKey.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "gun_model_type")),
            DataComponentType.<Integer>builder()
                .persistent(com.mojang.serialization.Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build()
        );
        
        // 同步到 GunWithMagItem 中的静态字段
        GunWithMagItem.HAS_MAGAZINE = HAS_MAGAZINE;
        GunWithMagItem.GUN_MODEL_TYPE = GUN_MODEL_TYPE;
    }
    //?}
}
