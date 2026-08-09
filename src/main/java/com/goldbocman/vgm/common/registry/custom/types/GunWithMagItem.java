package com.goldbocman.vgm.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
//? if >=1.20.5 {
/*import net.minecraft.core.component.DataComponentType;
*///?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.11 {
/*import net.minecraft.world.item.component.TooltipDisplay;
*///?}
import net.minecraft.world.level.Level;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.network.NetworkUtils;
import com.goldbocman.vgm.common.registry.NetworkReg.ItemComponentSyncPayload;
import com.goldbocman.vgm.common.registry.SoundReg;
import com.goldbocman.vgm.common.registry.TagReg;
import com.goldbocman.vgm.common.registry.custom.MagazineItem;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class GunWithMagItem extends GunItem {
    //? if >=1.20.5 {
    /*public static DataComponentType<Boolean> HAS_MAGAZINE;
    public static DataComponentType<Integer> GUN_MODEL_TYPE;
    *///?}

    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/GunWithMagItem");
    private final TagKey<Item> compatibleMagazines;

    // 弹药类型显示方法

    // 弹药类型显示方法
    //? if >=1.21.11 {
    /*@Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay tooltipComponent, Consumer<Component> tooltipAdder, @NotNull TooltipFlag type) {
        super.appendHoverText(stack, context, tooltipComponent, tooltipAdder, type);
        bren$appendHoverText(stack, tooltipAdder::accept);
    }
    *///?} else {
    //? if >=1.20.5 {
    /*@Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, java.util.@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag type) {
        super.appendHoverText(stack, context, tooltipComponents, type);
        bren$appendHoverText(stack, tooltipComponents::add);
    }
    *///?} else {
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, java.util.@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag type) {
        super.appendHoverText(stack, level, tooltipComponents, type);
        bren$appendHoverText(stack, tooltipComponents::add);
    }
    //?}
    //?}

    private void bren$appendHoverText(ItemStack stack, Consumer<Component> tooltipAdder) {
        // 显示当前弹匣信息
        if (hasMagazine(stack)) {
            var nbt = GunApiCompat.getTag(stack);
            String magItemId = GunApiCompat.getString(nbt, MAGAZINE_ITEM_KEY);
            if (!magItemId.isEmpty()) {
                var itemId = ResourceLocation.tryParse(magItemId);
                if (itemId != null) {
                    var item = GunApiCompat.getItem(itemId);
                    if (item != null) {
                        String magName = item.getName(stack).getString();
                        tooltipAdder.accept(Component.literal("§9Magazine: " + magName).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }
    }

    private static final String HAS_MAGAZINE_KEY = "HasMagazine";
    private static final String MAGAZINE_CAPACITY_KEY = "MagazineCapacity";
    private static final String MAGAZINE_CONTENTS_KEY = "MagazineContents";
    private static final String MAGAZINE_ITEM_KEY = "MagazineItem";


    // 修改构造函数，接收Item.Settings参数
    public GunWithMagItem(Properties settings) {
        this(settings, TagReg.MEDIUM_MAGAZINES);
    }

    public GunWithMagItem(Properties settings, TagKey<Item> compatibleMagazines) {
        super(settings);
        this.compatibleMagazines = compatibleMagazines;
    }

    @Override
    public void onReload(Player player) {
        ItemStack stack = player.getMainHandItem();
        ItemCooldowns cooldownManager = player.getCooldowns();

        if (stack.getItem() instanceof GunWithMagItem gunItem) {
            if (player instanceof IGunUser gunUser && !GunApiCompat.isOnCooldown(cooldownManager, stack)) {
                ItemStack mag = Bren.getMagazineFromPlayer(player, gunItem.compatibleMagazines());

                // Case 1: gun has a magazine -> allow unloading (regardless of whether player has a new one)
                // Case 2: gun has no magazine and player has a new one -> allow loading
                // Case 3: gun has no magazine and player has none -> nothing to do
                boolean hasCurrentMagazine = GunWithMagItem.hasMagazine(stack);
                boolean hasNewMagazine = !mag.isEmpty() && MagazineItem.getContents(mag) > 0;

                if (!hasCurrentMagazine && !hasNewMagazine) {
                    return;
                }

                if (!gunUser.bren_1_21_1$canReload()) {
                    return;
                }

                gunUser.bren_1_21_1$setCanReload(false);
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.RELOADING);
                gunUser.bren_1_21_1$setReloadingGun(stack);
                GunApiCompat.addCooldown(cooldownManager, stack, this.reloadSpeed());
            }
        }
    }

    public static void putMagazine(ItemStack stack, ItemStack mag, Player player) {
        if (!(mag.getItem() instanceof MagazineItem)) {
            LOGGER.warn("Attempted to put non-magazine item into gun: {}", mag.getItem().toString());
            return;
        }

        int magContents = MagazineItem.getContents(mag);
        int magCapacity = MagazineItem.getMaxCapacity(mag);
        String magItemId = BuiltInRegistries.ITEM.getKey(mag.getItem()).toString();

        var nbt = GunApiCompat.getTag(stack);
        nbt.putBoolean(HAS_MAGAZINE_KEY, true);
        nbt.putInt(MAGAZINE_CAPACITY_KEY, magCapacity);
        nbt.putInt(MAGAZINE_CONTENTS_KEY, magContents);
        nbt.putString(MAGAZINE_ITEM_KEY, magItemId);
        GunApiCompat.setTag(stack, nbt);

        if (stack.getItem() instanceof GunWithMagItem) {
            GunWithMagItem gunItem = (GunWithMagItem) stack.getItem();
            if (player instanceof ServerPlayer serverPlayer) {
                // 使用带玩家参数的版本来同步到客户端
                gunItem.updateReloadState(stack, false, serverPlayer);
            } else {
                // 后备：使用不带玩家参数的版本
                gunItem.updateReloadState(stack, false);
            }
        }


    }

    public static boolean hasMagazine(ItemStack stack){
        var nbt = GunApiCompat.getTag(stack);
        return GunApiCompat.getBoolean(nbt, HAS_MAGAZINE_KEY);
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        var nbt = GunApiCompat.getTag(stack);
        int stored = GunApiCompat.getInt(nbt, MAGAZINE_CAPACITY_KEY);
        if (stored > 0) return stored;
        // No magazine loaded — return the capacity of this gun's compatible magazine type
        return this.compatibleMagazines.equals(TagReg.SHORT_MAGAZINES) ? 6 : 20;
    }

    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {
        if (matrices == null) return false;

        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return super.applyCustomMatrix(entity, state, matrices, stack, cooldownProgress, leftHanded);
        }

        if (state != GunHelper.GunStates.NORMAL) return false;

        float f2 = Math.max(1 - cooldownProgress, 0);
        matrices.translate(0, -f2 / 4 + 0.25F, f2 / 8 - 0.25F);

        return true;
    }

    @Override
    protected Component getAmmoDescription() {
        if (this.compatibleMagazines.equals(TagReg.SHORT_MAGAZINES)) {
            return Component.literal("Uses: Short Magazine").withStyle(ChatFormatting.YELLOW);
        }
        return Component.literal("Uses: Magazine").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public int getContents(ItemStack stack) {
        if (hasMagazine(stack)) {
            var nbt = GunApiCompat.getTag(stack);
            return GunApiCompat.getInt(nbt, MAGAZINE_CONTENTS_KEY);
        }
        return 0;
    }

    // Completes the reload started in onReload() once the reload cooldown finishes - restored from
    // commit 51853f2, where this file had the real implementation (unload-or-load semantics, magazine
    // sounds, empty-magazine cleanup). It was accidentally dropped during the CUSTOM_DATA ->
    // GunApiCompat migration for the 1.20.1 port, which regressed reload on every version/loader (not
    // just 1.20.1) since GunWithMagItem then silently inherited GunItem's no-op reloadTick() default -
    // canReload/gunState (set false/RELOADING in onReload) were never reset, permanently locking the
    // player out of firing or reloading ANY gun after the very first reload press.
    @Override
    public void reloadTick(ItemStack stack, Level world, Player player, IGunUser gunUser) {
        ItemCooldowns cooldownManager = player.getCooldowns();
        float cooldownProgress = GunApiCompat.getCooldownPercent(cooldownManager, stack, 1.0F);

        if (!GunApiCompat.isOnCooldown(cooldownManager, stack) &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {

            boolean reloadSuccess = false;

            ItemStack newMag = Bren.getMagazineFromPlayer(player, ((GunWithMagItem) stack.getItem()).compatibleMagazines());
            boolean hasNewMagazine = !newMag.isEmpty() && MagazineItem.getContents(newMag) > 0;
            boolean hasCurrentMagazine = GunWithMagItem.hasMagazine(stack);

            // Case 1: gun has a magazine -> unload it (regardless of whether player has a new one)
            if (hasCurrentMagazine) {
                ItemStack removedMag = GunWithMagItem.unloadMagazine(stack, player);
                if (!removedMag.isEmpty()) {
                    world.playSound(null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundReg.ITEM_MAGAZINE_REMOVE,
                            SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
                    reloadSuccess = true;
                } else {
                    LOGGER.warn("Failed to unload magazine for player {}", player.getName().getString());
                }
            }
            // Case 2: gun has no magazine and player has one -> load it
            else if (hasNewMagazine) {
                GunWithMagItem.putMagazine(stack, newMag, player);
                newMag.shrink(1);
                reloadSuccess = true;

                world.playSound(null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundReg.ITEM_MAGAZINE_INSERT,
                        SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
            }
            // Case 3: neither -> nothing to do

            if (!reloadSuccess) {
                LOGGER.warn("Reload failed for player {}, resetting state to NORMAL to prevent stuck",
                        player.getName().getString());
            }
            gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
            gunUser.bren_1_21_1$setCanReload(true);
            gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
        } else if (cooldownProgress == 0.75F &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {
            world.playSound(null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundReg.ITEM_MAGAZINE_INSERT,
                    SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
        }

        checkAndUnloadEmptyMagazine(stack, player);
    }

    private void checkAndUnloadEmptyMagazine(ItemStack stack, Player player) {
        var nbt = GunApiCompat.getTag(stack);

        if (nbt.contains("EmptyMagazineItem")) {
            String magItemId = GunApiCompat.getString(nbt, "EmptyMagazineItem");
            if (!magItemId.isEmpty()) {
                var itemId = ResourceLocation.tryParse(magItemId);
                if (itemId != null) {
                    var item = GunApiCompat.getItem(itemId);
                    if (item instanceof MagazineItem) {
                        ItemStack emptyMag = new ItemStack(item);

                        if (!player.getInventory().add(emptyMag)) {
                            player.drop(emptyMag, false);
                        }

                        nbt.remove("EmptyMagazineItem");
                        GunApiCompat.setTag(stack, nbt);
                    }
                }
            }
        }
    }

    public static ItemStack unloadMagazine(ItemStack stack, Player player) {
        if (hasMagazine(stack)) {
            var nbt = GunApiCompat.getTag(stack);

            // 修复：正确获取弹匣物品ID
            String magItemId = GunApiCompat.getString(nbt, MAGAZINE_ITEM_KEY);
            int remainingContents = GunApiCompat.getInt(nbt, MAGAZINE_CONTENTS_KEY);

            // 清除弹匣信息
            nbt.putBoolean(HAS_MAGAZINE_KEY, false);
            nbt.putInt(MAGAZINE_CAPACITY_KEY, 0);
            nbt.putInt(MAGAZINE_CONTENTS_KEY, 0);
            nbt.remove(MAGAZINE_ITEM_KEY);
            GunApiCompat.setTag(stack, nbt);
            // 更新模型状态
            if (stack.getItem() instanceof GunWithMagItem && player instanceof ServerPlayer serverPlayer) {
                ((GunWithMagItem) stack.getItem()).updateReloadState(stack, false, serverPlayer); // 不是装弹状态，而是根据弹匣状态更新
            }


            // 返回空弹匣给玩家（如果有剩余子弹则保留）
            if (!magItemId.isEmpty()) {
                var itemId = ResourceLocation.tryParse(magItemId);
                if (itemId != null) {
                    var item = GunApiCompat.getItem(itemId);
                    if (item instanceof MagazineItem) {
                        ItemStack emptyMag = new ItemStack(item);

                        // 修复：确保正确处理剩余子弹
                        if (remainingContents > 0) {
                            MagazineItem.fillMagazine(emptyMag, remainingContents);
                        }

                        // 修复：使用更可靠的物品返还方法
                        if (!player.getInventory().add(emptyMag)) {
                            // 如果无法放入物品栏，则尝试giveItemStack方法
                            if (!player.addItem(emptyMag)) {
                                // 如果还是无法给予，则掉落在地上
                                player.drop(emptyMag, false);
                            }
                        }
                        return emptyMag; // 正确返回
                    } else {
                        LOGGER.warn("Item {} is not a MagazineItem", magItemId);
                    }
                } else {
                    LOGGER.warn("Invalid magazine item ID: {}", magItemId);
                }
            } else {
                LOGGER.warn("No magazine item ID found in NBT");
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void useBullet(ItemStack stack) {
        if (hasMagazine(stack)) {
            var nbt = GunApiCompat.getTag(stack);
            int currentContents = GunApiCompat.getInt(nbt, MAGAZINE_CONTENTS_KEY);
            int newContents = Math.max(currentContents - 1, 0);
            nbt.putInt(MAGAZINE_CONTENTS_KEY, newContents);

            // 修复：即使弹匣空了，也保持HAS_MAGAZINE_KEY为true，允许玩家卸下空弹匣
            GunApiCompat.setTag(stack, nbt);
        }
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        if (!hasMagazine(stack)) {
            return true;
        }
        return getContents(stack) <= 0;
    }

    //? if >=1.21.11 {
    /*public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        bren$inventoryTick(stack, entity);
        super.inventoryTick(stack, world, entity, slot);
    }
    *///?} else {
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        bren$inventoryTick(stack, entity);
        super.inventoryTick(stack, world, entity, slot, selected);
    }
    //?}

    // Old and new inventoryTick overloads report the ticking slot differently (EquipmentSlot enum vs
    // raw index + "selected" boolean) - checking whether stack is actually the held item works
    // identically on both and reproduces the original "mainhand or offhand only" filter.
    private void bren$inventoryTick(ItemStack stack, Entity entity) {
        if (entity instanceof IGunUser gunUser && entity instanceof Player player
                && (stack == player.getMainHandItem() || stack == player.getOffhandItem())) {
            // Only re-affirm the reference to the gun that's ALREADY reloading (stack identity is
            // normally stable tick-to-tick for the same held item, so this is a safe no-op in the
            // common case) - must not blindly redirect to whatever is now held. If the player swaps
            // active hotbar slot to a different mag-gun while gunState is still RELOADING (leftover
            // from the first gun's in-flight reload), this used to overwrite reloadingGun with the
            // newly-held gun, so once the cooldown expired server-side, reloadTick() ran against the
            // WRONG gun - one that was never actually put on cooldown, firing instantly.
            if (gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)
                    && stack == gunUser.bren_1_21_1$getReloadingGun()) {
                gunUser.bren_1_21_1$setReloadingGun(stack);
            }
        }
    }

    /**
     * 更新枪械的弹匣状态
     */
    public void updateReloadState(ItemStack stack, boolean isReloading) {
        // 使用 DataComponentType 控制模型切换
        // GUN_MODEL_TYPE = 1: 有弹匣状态，显示 ak47 模型
        // GUN_MODEL_TYPE = 0 或不存在：无弹匣状态，显示 ak47_empty 模型

        // HAS_MAGAZINE (the DataComponentType) doesn't exist before 1.20.5 - and is only actually read
        // for display purposes on >=1.21.11's Item Model Definition select-tree anyway (<1.21.11,
        // including 1.20.1, already reads magazine state straight from NBT via
        // LegacyItemPropertiesAccessor - see claude/gun-rendering.md), so this is a genuine no-op on
        // 1.20.1, not a partial reimplementation.
        //? if >=1.20.5 {
        /*boolean hasMagazine = GunWithMagItem.hasMagazine(stack);

        if (hasMagazine) {
            // 有弹匣时设置 GUN_MODEL_TYPE 为 1，显示 ak47 模型
            try {
                stack.set(HAS_MAGAZINE, true);
            } catch (Exception e) {
                LOGGER.error("Failed to set GUN_MODEL_TYPE", e);
            }
        } else {
            // 无弹匣时移除 GUN_MODEL_TYPE 或设置为 0，显示 ak47_empty 模型
            try {
                stack.remove(HAS_MAGAZINE);
            } catch (Exception e) {
                LOGGER.error("Failed to remove GUN_MODEL_TYPE", e);
            }
        }
        *///?}
    }

    // 重载版本，带有玩家信息，用于发送网络同步包
    public void updateReloadState(ItemStack stack, boolean isReloading, ServerPlayer player) {
        // 先执行基本的更新
        updateReloadState(stack, isReloading);

        // 同步到客户端
        syncModelStateToClient(stack, GunWithMagItem.hasMagazine(stack), player);
    }

    // 向客户端同步模型状态
    private void syncModelStateToClient(ItemStack stack, boolean hasMagazine, ServerPlayer player) {
        if (player != null) {
            // 发送网络包同步物品组件状态
            ItemComponentSyncPayload payload = new ItemComponentSyncPayload(-1, hasMagazine); // -1 表示任意槽位，或可指定特定槽位
            NetworkUtils.sendToPlayer(player, payload);
        }
    }


    public TagKey<Item> compatibleMagazines() {
        return this.compatibleMagazines;
    }
}
