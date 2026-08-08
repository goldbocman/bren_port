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

    //? if >=1.21.11 {
    public static DamageSource of(Level world, ResourceKey<DamageType> key) {
        return world.damageSources().source(key);
    }

    public static DamageSource shot(Level world, @Nullable Entity source, @Nullable Entity attacker) {
        return world.damageSources().source(BULLET_TYPE, source, attacker);
    }
    //?} else {
    /*// DamageSources has no public generic source(ResourceKey<DamageType>, ...) method pre-1.21.11 -
    // only per-vanilla-cause named methods (inFire(), lava(), etc.) - so a custom damage type has to
    // resolve its own Holder<DamageType> and go through DamageSource's constructor directly instead.
    public static DamageSource of(Level world, ResourceKey<DamageType> key) {
        var holder = world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
        return new DamageSource(holder);
    }

    public static DamageSource shot(Level world, @Nullable Entity source, @Nullable Entity attacker) {
        var holder = world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(BULLET_TYPE);
        return new DamageSource(holder, source, attacker);
    }
    *///?}
}