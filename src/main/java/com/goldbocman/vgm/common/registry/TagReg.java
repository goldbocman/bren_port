package com.goldbocman.vgm.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import com.goldbocman.vgm.common.Bren;

public class TagReg {
    public static final TagKey<Item> MEDIUM_MAGAZINES = itemTag("magazines/medium_magazines");
    public static final TagKey<Item> SHORT_MAGAZINES = itemTag("magazines/short_magazines");
    public static final TagKey<Item> LARGE_MAGAZINES = itemTag("magazines/large_magazines");
    public static final TagKey<DamageType> IS_BULLET = damageTypeTag("is_bullet");

    public static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Bren.MODID, name));
    }

    public static TagKey<DamageType> damageTypeTag(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Bren.MODID, name));
    }
}
