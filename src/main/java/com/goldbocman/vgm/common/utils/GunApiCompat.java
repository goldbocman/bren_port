package com.goldbocman.vgm.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;

// Small bridge over vanilla APIs that changed shape between 1.21.1 and 26.2.x/1.21.11, used by the
// gun item classes so the actual gameplay logic doesn't need per-call-site version gating:
// - ItemCooldowns keys cooldowns by plain Item pre-1.21.11, not ItemStack/Identifier.
// - CompoundTag's getInt/getString/getBoolean return the primitive directly pre-1.21.11, not an
//   Optional-like wrapper.
// - DefaultedRegistry's lookup-by-id method is named get(...), not getValue(...), pre-1.21.11.
public class GunApiCompat {

    public static boolean isOnCooldown(ItemCooldowns cooldowns, ItemStack stack) {
        //? if >=1.21.11 {
        return cooldowns.isOnCooldown(stack);
        //?} else {
        /*return cooldowns.isOnCooldown(stack.getItem());
        *///?}
    }

    public static float getCooldownPercent(ItemCooldowns cooldowns, ItemStack stack, float partialTick) {
        //? if >=1.21.11 {
        return cooldowns.getCooldownPercent(stack, partialTick);
        //?} else {
        /*return cooldowns.getCooldownPercent(stack.getItem(), partialTick);
        *///?}
    }

    public static void addCooldown(ItemCooldowns cooldowns, ItemStack stack, int ticks) {
        //? if >=1.21.11 {
        cooldowns.addCooldown(BuiltInRegistries.ITEM.getKey(stack.getItem()), ticks);
        //?} else {
        /*cooldowns.addCooldown(stack.getItem(), ticks);
        *///?}
    }

    public static int getInt(CompoundTag nbt, String key) {
        return getInt(nbt, key, 0);
    }

    public static int getInt(CompoundTag nbt, String key, int fallback) {
        //? if >=1.21.11 {
        return nbt.getInt(key).orElse(fallback);
        //?} else {
        /*return nbt.contains(key) ? nbt.getInt(key) : fallback;
        *///?}
    }

    public static String getString(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        return nbt.getString(key).orElse("");
        //?} else {
        /*return nbt.getString(key);
        *///?}
    }

    public static boolean getBoolean(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        return nbt.getBoolean(key).orElse(false);
        //?} else {
        /*return nbt.getBoolean(key);
        *///?}
    }

    public static double getDouble(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        return nbt.getDouble(key).orElse(0.0);
        //?} else {
        /*return nbt.getDouble(key);
        *///?}
    }

    // Caller must already know the compound is present (e.g. via nbt.contains(key)) - pre-1.21.11
    // getCompound(...) returns an empty CompoundTag rather than an empty Optional when missing.
    public static CompoundTag getCompound(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        return nbt.getCompound(key).orElseThrow();
        //?} else {
        /*return nbt.getCompound(key);
        *///?}
    }

    public static float getPartialTick(Minecraft client) {
        //? if >=1.21.11 {
        return client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        //?} else {
        /*// Minecraft.getDeltaTracker() is named getTimer() pre-1.21.11 - same DeltaTracker class either way.
        return client.getTimer().getGameTimeDeltaPartialTick(true);
        *///?}
    }

    public static Item getItem(Identifier id) {
        //? if >=1.21.11 {
        return BuiltInRegistries.ITEM.getValue(id);
        //?} else {
        /*return BuiltInRegistries.ITEM.get(id);
        *///?}
    }
}
