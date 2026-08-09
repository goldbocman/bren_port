package com.goldbocman.vgm.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
//? if >=1.20.5 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///?}
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;

// Small bridge over vanilla APIs that changed shape between 1.21.1 and 26.2.x/1.21.11, used by the
// gun item classes so the actual gameplay logic doesn't need per-call-site version gating:
// - ItemCooldowns keys cooldowns by plain Item pre-1.21.11, not ItemStack/ResourceLocation.
// - CompoundTag's getInt/getString/getBoolean return the primitive directly pre-1.21.11, not an
//   Optional-like wrapper.
// - DefaultedRegistry's lookup-by-id method is named get(...), not getValue(...), pre-1.21.11.
// - getTag/setTag: DataComponents.CUSTOM_DATA doesn't exist before 1.20.5 at all - stack-level NBT
//   storage is stack.getOrCreateTag()/setTag(CompoundTag) there instead. Every gun/magazine item
//   class uses these two methods instead of touching DataComponents.CUSTOM_DATA directly, so the
//   version split lives in exactly one place instead of ~15-20 call sites across the mod.
public class GunApiCompat {

    public static CompoundTag getTag(ItemStack stack) {
        //? if >=1.20.5 {
        /*return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        *///?} else {
        return stack.getOrCreateTag();
        //?}
    }

    public static void setTag(ItemStack stack, CompoundTag nbt) {
        //? if >=1.20.5 {
        /*stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        *///?} else {
        stack.setTag(nbt);
        //?}
    }

    public static boolean isOnCooldown(ItemCooldowns cooldowns, ItemStack stack) {
        //? if >=1.21.11 {
        /*return cooldowns.isOnCooldown(stack);
        *///?} else {
        return cooldowns.isOnCooldown(stack.getItem());
        //?}
    }

    public static float getCooldownPercent(ItemCooldowns cooldowns, ItemStack stack, float partialTick) {
        //? if >=1.21.11 {
        /*return cooldowns.getCooldownPercent(stack, partialTick);
        *///?} else {
        return cooldowns.getCooldownPercent(stack.getItem(), partialTick);
        //?}
    }

    public static void addCooldown(ItemCooldowns cooldowns, ItemStack stack, int ticks) {
        //? if >=1.21.11 {
        /*cooldowns.addCooldown(BuiltInRegistries.ITEM.getKey(stack.getItem()), ticks);
        *///?} else {
        cooldowns.addCooldown(stack.getItem(), ticks);
        //?}
    }

    // removeCooldown(...) triggers onCooldownEnded(...) internally, which on the server's
    // ServerItemCooldowns subclass sends a ClientboundCooldownPacket with duration 0 - confirmed via
    // javap -c bytecode. So calling this server-side is enough by itself to also clear the client's
    // hotbar cooldown swirl; no manual sync packet needed.
    public static void removeCooldown(ItemCooldowns cooldowns, ItemStack stack) {
        //? if >=1.21.11 {
        /*cooldowns.removeCooldown(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        *///?} else {
        cooldowns.removeCooldown(stack.getItem());
        //?}
    }

    public static int getInt(CompoundTag nbt, String key) {
        return getInt(nbt, key, 0);
    }

    public static int getInt(CompoundTag nbt, String key, int fallback) {
        //? if >=1.21.11 {
        /*return nbt.getInt(key).orElse(fallback);
        *///?} else {
        return nbt.contains(key) ? nbt.getInt(key) : fallback;
        //?}
    }

    public static String getString(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        /*return nbt.getString(key).orElse("");
        *///?} else {
        return nbt.getString(key);
        //?}
    }

    public static boolean getBoolean(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        /*return nbt.getBoolean(key).orElse(false);
        *///?} else {
        return nbt.getBoolean(key);
        //?}
    }

    public static double getDouble(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        /*return nbt.getDouble(key).orElse(0.0);
        *///?} else {
        return nbt.getDouble(key);
        //?}
    }

    // Caller must already know the compound is present (e.g. via nbt.contains(key)) - pre-1.21.11
    // getCompound(...) returns an empty CompoundTag rather than an empty Optional when missing.
    public static CompoundTag getCompound(CompoundTag nbt, String key) {
        //? if >=1.21.11 {
        /*return nbt.getCompound(key).orElseThrow();
        *///?} else {
        return nbt.getCompound(key);
        //?}
    }

    public static float getPartialTick(Minecraft client) {
        //? if >=1.21.11 {
        /*return client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        *///?} else {
        //? if >=1.20.5 {
        /*// Minecraft.getDeltaTracker() is named getTimer() pre-1.21.11 - same DeltaTracker class either way.
        return client.getTimer().getGameTimeDeltaPartialTick(true);
        *///?} else {
        // No DeltaTracker/Timer accessor at all before 1.20.5 (Minecraft.timer is private there,
        // confirmed via javap) - Minecraft.getFrameTime() is the classic pre-1.20.5 direct equivalent.
        return client.getFrameTime();
        //?}
        //?}
    }

    public static Item getItem(ResourceLocation id) {
        //? if >=1.21.11 {
        /*return BuiltInRegistries.ITEM.getValue(id);
        *///?} else {
        return BuiltInRegistries.ITEM.get(id);
        //?}
    }

    // Enchantments became datapack registry entries (looked up as Holder<Enchantment> via
    // RegistryAccess) at 1.20.5 - before that, Enchantments.FLAME/.PUNCH are plain Enchantment
    // objects usable directly, AND the constant names themselves differ (FLAMING_ARROWS/PUNCH_ARROWS,
    // not FLAME/PUNCH - confirmed via javap, not assumed from the newer names' obvious-looking
    // shortenings).
    public static int getFlameLevel(net.minecraft.world.level.Level level, ItemStack stack) {
        //? if >=1.20.5 {
        /*return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME),
                stack);
        *///?} else {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.minecraft.world.item.enchantment.Enchantments.FLAMING_ARROWS, stack);
        //?}
    }

    public static int getPunchLevel(net.minecraft.world.level.Level level, ItemStack stack) {
        //? if >=1.20.5 {
        /*return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PUNCH),
                stack);
        *///?} else {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.minecraft.world.item.enchantment.Enchantments.PUNCH_ARROWS, stack);
        //?}
    }
}
