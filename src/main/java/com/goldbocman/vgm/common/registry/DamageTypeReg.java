package com.goldbocman.vgm.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import com.goldbocman.vgm.common.Bren;
import org.jetbrains.annotations.Nullable;

public class DamageTypeReg {
    public static final ResourceKey<DamageType> BULLET_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Bren.MODID, "bullet_type"));

    public static DamageSource of(Level world, ResourceKey<DamageType> key) {
        // 修复：在Minecraft 1.21.4中使用getDamageSources().create()方法
        return world.damageSources().source(key);
    }

    public static DamageSource shot(Level world, @Nullable Entity source, @Nullable Entity attacker) {
        // 修复：在Minecraft 1.21.4中使用getDamageSources().create()方法
        return world.damageSources().source(BULLET_TYPE, source, attacker);
    }
}