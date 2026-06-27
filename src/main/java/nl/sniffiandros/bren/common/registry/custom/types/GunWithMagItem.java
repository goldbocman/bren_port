package nl.sniffiandros.bren.common.registry.custom.types;

import nl.sniffiandros.bren.common.registry.NetworkReg.ItemComponentSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.SoundReg;
import nl.sniffiandros.bren.common.registry.TagReg;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;
import nl.sniffiandros.bren.common.utils.GunHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class GunWithMagItem extends GunItem {
    public static DataComponentType<Boolean> HAS_MAGAZINE;
    public static DataComponentType<Integer> GUN_MODEL_TYPE;

    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/GunWithMagItem");
    private final TagKey<Item> compatibleMagazines;

    // 弹药类型显示方法

    // 弹药类型显示方法
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay tooltipComponent, Consumer<Component> tooltipAdder, @NotNull TooltipFlag type) {
        super.appendHoverText(stack, context, tooltipComponent, tooltipAdder, type);

        // 显示当前弹匣信息
        if (hasMagazine(stack)) {
            var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY).copyTag();
            String magItemId = nbt.getString(MAGAZINE_ITEM_KEY).orElse("");
            if (!magItemId.isEmpty()) {
                var itemId = Identifier.tryParse(magItemId);
                if (itemId != null) {
                    var item = BuiltInRegistries.ITEM.getValue(itemId);
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

        LOGGER.info("onReload called for player: {}, item: {}, cooling down: {}",
                player.getName().getString(), stack.getItem().toString(), cooldownManager.isOnCooldown(stack));

        if (stack.getItem() instanceof GunWithMagItem gunItem) {
            if (player instanceof IGunUser gunUser && !cooldownManager.isOnCooldown(stack)) {
                ItemStack mag = Bren.getMagazineFromPlayer(player, gunItem.compatibleMagazines());

                LOGGER.info("Player {} has magazine: {}, current magazine in gun: {}",
                        player.getName().getString(), !mag.isEmpty(), hasMagazine(stack));

                // 修复：正确判断是否可以操作
                // 情况1：枪械有弹匣 → 允许卸下（无论玩家是否有新弹匣）
                // 情况2：枪械无弹匣且玩家有新弹匣 → 允许装填
                // 情况3：枪械无弹匣且玩家无新弹匣 → 无法操作
                boolean hasCurrentMagazine = GunWithMagItem.hasMagazine(stack);
                boolean hasNewMagazine = !mag.isEmpty() && MagazineItem.getContents(mag) > 0;

                if (!hasCurrentMagazine && !hasNewMagazine) {
                    LOGGER.info("No magazine available and no magazine to unload for player {}", player.getName().getString());
                    return; // 既没有弹匣可装，也没有弹匣可卸
                }

                if (!gunUser.bren_1_21_1$canReload()) {
                    LOGGER.info("Player {} cannot reload (canReload=false)", player.getName().getString());
                    return;
                }

                LOGGER.info("Starting reload process for player {}, gun state: {}",
                        player.getName().getString(), gunUser.bren_1_21_1$getGunState());

                gunUser.bren_1_21_1$setCanReload(false);
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.RELOADING);
                gunUser.bren_1_21_1$setReloadingGun(stack); // 关键修复：设置reloadingGun
                // 修复：在Minecraft 1.21.4中，set方法需要Identifier而不是Item
                var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                cooldownManager.addCooldown(itemId, this.reloadSpeed());

                LOGGER.info("Reload started for player {}, cooldown set for item: {}, speed: {}",
                        player.getName().getString(), itemId, this.reloadSpeed());
            } else {
                LOGGER.info("Player {} cannot reload: is IGunUser: {}, cooling down: {}",
                        player.getName().getString(), player instanceof IGunUser, cooldownManager.isOnCooldown(stack));
            }
        } else {
            LOGGER.info("Main hand item is not GunWithMagItem: {}", stack.getItem().toString());
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

        LOGGER.info("Putting magazine into gun: item={}, capacity={}, contents={}",
                magItemId, magCapacity, magContents);

        var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                CustomData.EMPTY).copyTag();
        nbt.putBoolean(HAS_MAGAZINE_KEY, true);
        nbt.putInt(MAGAZINE_CAPACITY_KEY, magCapacity);
        nbt.putInt(MAGAZINE_CONTENTS_KEY, magContents);
        nbt.putString(MAGAZINE_ITEM_KEY, magItemId);
        stack.set(DataComponents.CUSTOM_DATA,
                CustomData.of(nbt));

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
        var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                CustomData.EMPTY).copyTag();
        return nbt.getBoolean(HAS_MAGAZINE_KEY).orElse(false);
    }

    @Override
    public int getMaxCapacity(ItemStack stack) {
        var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                CustomData.EMPTY).copyTag();
        int stored = nbt.getInt(MAGAZINE_CAPACITY_KEY).orElse(0);
        if (stored > 0) return stored;
        // No magazine loaded — return the capacity of this gun's compatible magazine type
        return this.compatibleMagazines.equals(TagReg.SHORT_MAGAZINES) ? 6 : 20;
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
            var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY).copyTag();
            return nbt.getInt(MAGAZINE_CONTENTS_KEY).orElse(0);
        }
        return 0;
    }

    public static ItemStack unloadMagazine(ItemStack stack, Player player) {
        if (hasMagazine(stack)) {
            var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY).copyTag();

            // 修复：正确获取弹匣物品ID
            String magItemId = nbt.getString(MAGAZINE_ITEM_KEY).orElse("");
            int remainingContents = nbt.getInt(MAGAZINE_CONTENTS_KEY).orElse(0);

            LOGGER.info("Unloading magazine from gun: item={}, remaining contents={}",
                    magItemId, remainingContents);

            // 清除弹匣信息
            nbt.putBoolean(HAS_MAGAZINE_KEY, false);
            nbt.putInt(MAGAZINE_CAPACITY_KEY, 0);
            nbt.putInt(MAGAZINE_CONTENTS_KEY, 0);
            nbt.remove(MAGAZINE_ITEM_KEY);
            stack.set(DataComponents.CUSTOM_DATA,
                    CustomData.of(nbt));
            // 更新模型状态
            if (stack.getItem() instanceof GunWithMagItem && player instanceof ServerPlayer serverPlayer) {
                ((GunWithMagItem) stack.getItem()).updateReloadState(stack, false, serverPlayer); // 不是装弹状态，而是根据弹匣状态更新
            }


            // 返回空弹匣给玩家（如果有剩余子弹则保留）
            if (!magItemId.isEmpty()) {
                var itemId = Identifier.tryParse(magItemId);
                if (itemId != null) {
                    var item = BuiltInRegistries.ITEM.getValue(itemId);
                    if (item instanceof MagazineItem) {
                        ItemStack emptyMag = new ItemStack(item);

                        // 修复：确保正确处理剩余子弹
                        if (remainingContents > 0) {
                            MagazineItem.fillMagazine(emptyMag, remainingContents);
                        }

                        // 修复：使用更可靠的物品返还方法
                        if (player.getInventory().add(emptyMag)) {
                            LOGGER.info("Magazine returned to player inventory: {}", magItemId);
                        } else {
                            // 如果无法放入物品栏，则尝试giveItemStack方法
                            if (!player.addItem(emptyMag)) {
                                // 如果还是无法给予，则掉落在地上
                                player.drop(emptyMag, false);
                                LOGGER.info("Magazine dropped on ground: {}", magItemId);
                            } else {
                                LOGGER.info("Magazine given to player: {}", magItemId);
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
        } else {
            LOGGER.info("No magazine to unload from gun");
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void useBullet(ItemStack stack) {
        if (hasMagazine(stack)) {
            var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY).copyTag();
            int currentContents = nbt.getInt(MAGAZINE_CONTENTS_KEY).orElse(0);
            int newContents = Math.max(currentContents - 1, 0);
            nbt.putInt(MAGAZINE_CONTENTS_KEY, newContents);

            LOGGER.info("Using bullet from magazine: current={}, new={}", currentContents, newContents);

            // 修复：即使弹匣空了，也保持HAS_MAGAZINE_KEY为true，允许玩家卸下空弹匣
            if (newContents <= 0) {
                LOGGER.info("Magazine is empty, but keeping magazine in gun for manual unloading");
                // 不再设置HAS_MAGAZINE_KEY为false，保持弹匣在枪械中
            }

            stack.set(DataComponents.CUSTOM_DATA,
                    CustomData.of(nbt));
        } else {
            LOGGER.info("No magazine available for using bullet");
        }
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        if (!hasMagazine(stack)) {
            return true;
        }
        return getContents(stack) <= 0;
    }

    @Override
    public void reloadTick(ItemStack stack, Level world, Player player, IGunUser gunUser) {
        ItemCooldowns cooldownManager = player.getCooldowns();
        float cooldownProgress = cooldownManager.getCooldownPercent(stack, 1.0F);

        LOGGER.debug("reloadTick called: player={}, state={}, cooling down={}, progress={}",
                player.getName().getString(), gunUser.bren_1_21_1$getGunState(),
                cooldownManager.isOnCooldown(stack), cooldownProgress);

        // 关键修复：确保只有在装弹状态下才执行装弹逻辑
        if (!cooldownManager.isOnCooldown(stack) &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {

            LOGGER.info("Reload tick processing for player {}", player.getName().getString());
            boolean reloadSuccess = false;

            // 检查玩家是否有新弹匣
            ItemStack newMag = Bren.getMagazineFromPlayer(player, ((GunWithMagItem) stack.getItem()).compatibleMagazines());
            boolean hasNewMagazine = !newMag.isEmpty() && MagazineItem.getContents(newMag) > 0;
            boolean hasCurrentMagazine = GunWithMagItem.hasMagazine(stack);

            LOGGER.info("Player {} has new magazine: {}, current magazine in gun: {}",
                    player.getName().getString(), hasNewMagazine, hasCurrentMagazine);

            // 情况1：枪械有弹匣 → 卸下弹匣（无论玩家是否有新弹匣）
            if (hasCurrentMagazine) {
                LOGGER.info("Magazine unloading for player {}", player.getName().getString());

                // 卸下弹匣
                ItemStack removedMag = GunWithMagItem.unloadMagazine(stack, player);
                if (!removedMag.isEmpty()) {
                    world.playSound(null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundReg.ITEM_MAGAZINE_REMOVE,
                            SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
                    reloadSuccess = true;
                    LOGGER.info("Magazine unloaded successfully for player {}", player.getName().getString());
                } else {
                    LOGGER.warn("Failed to unload magazine for player {}", player.getName().getString());
                }
            }
            // 情况2：枪械无弹匣且物品栏有弹匣 → 装填弹匣
            else if (!hasCurrentMagazine && hasNewMagazine) {
                LOGGER.info("Magazine loading for player {}: contents={}",
                        player.getName().getString(), MagazineItem.getContents(newMag));

                GunWithMagItem.putMagazine(stack, newMag, player);
                newMag.shrink(1);
                reloadSuccess = true;

                world.playSound(null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundReg.ITEM_MAGAZINE_INSERT,
                        SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
                LOGGER.info("Magazine loaded successfully for player {}", player.getName().getString());
            }
            // 情况3：枪械无弹匣且物品栏无弹匣 → 无法操作
            else {
                LOGGER.info("No valid operation for player {}: gun has no magazine and player has no magazine",
                        player.getName().getString());
            }

            // 关键修复：确保状态重置逻辑正确
            if (reloadSuccess) {
                LOGGER.info("Reload successful for player {}, resetting state to NORMAL", player.getName().getString());
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
                gunUser.bren_1_21_1$setCanReload(true);
                gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
            } else {
                // 如果装弹失败，也需要重置状态以避免卡死
                LOGGER.warn("Reload failed for player {}, resetting state to NORMAL to prevent stuck",
                        player.getName().getString());
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
                gunUser.bren_1_21_1$setCanReload(true);
                gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
            }
        } else if (cooldownProgress == 0.75F &&
                gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {
            // 修复：在冷却进度达到75%时播放装弹声音
            LOGGER.info("Playing reload sound at 75% progress for player {}", player.getName().getString());
            world.playSound(null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundReg.ITEM_MAGAZINE_INSERT,
                    SoundSource.PLAYERS, 1.0F, 1.0F - (player.getRandom().nextFloat() - 0.5F) / 4);
        }

        // 在装弹逻辑执行完成后，再检查是否有标记为需要卸下的空弹匣
        checkAndUnloadEmptyMagazine(stack, player);
    }

    /**
     * 检查并卸下标记为空的弹匣
     */
    private void checkAndUnloadEmptyMagazine(ItemStack stack, Player player) {
        var nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                CustomData.EMPTY).copyTag();

        if (nbt.contains("EmptyMagazineItem")) {
            String magItemId = nbt.getString("EmptyMagazineItem").orElse("");
            if (!magItemId.isEmpty()) {
                var itemId = Identifier.tryParse(magItemId);
                if (itemId != null) {
                    var item = BuiltInRegistries.ITEM.getValue(itemId);
                    if (item instanceof MagazineItem) {
                        ItemStack emptyMag = new ItemStack(item);
                        // 空弹匣没有子弹

                        if (!player.getInventory().add(emptyMag)) {
                            // 如果无法放入物品栏，则掉落在地上
                            player.drop(emptyMag, false);
                        }

                        // 清除标记
                        nbt.remove("EmptyMagazineItem");
                        stack.set(DataComponents.CUSTOM_DATA,
                                CustomData.of(nbt));

                        LOGGER.info("Empty magazine returned to player {}: item={}",
                                player.getName().getString(), magItemId);
                    }
                }
            }
        }
    }

    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        if (entity instanceof IGunUser gunUser && entity instanceof Player player) {
            // 检查当前装备槽是否为手持槽位
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                if (gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {
                    LOGGER.debug("Setting reloading gun for player {} in inventory tick", player.getName().getString());
                    gunUser.bren_1_21_1$setReloadingGun(stack);
                }

//                 定期检查是否需要卸下空弹匣
//                if (world.getTime() % 20 == 0) { // 每秒检查一次
//                    checkAndUnloadEmptyMagazine(stack, player);
//                }
            }
        }
        super.inventoryTick(stack, world, entity, slot);
    }

    /**
     * 更新枪械的弹匣状态
     */
    public void updateReloadState(ItemStack stack, boolean isReloading) {
        // 使用 DataComponentType 控制模型切换
        // GUN_MODEL_TYPE = 1: 有弹匣状态，显示 ak47 模型
        // GUN_MODEL_TYPE = 0 或不存在：无弹匣状态，显示 ak47_empty 模型

        boolean hasMagazine = GunWithMagItem.hasMagazine(stack);
        LOGGER.info("Updating reload state - hasMagazine: {}", hasMagazine);

        if (hasMagazine) {
            // 有弹匣时设置 GUN_MODEL_TYPE 为 1，显示 ak47 模型
            LOGGER.info("Setting GUN_MODEL_TYPE to 1 for gun with magazine");
            try {
                stack.set(HAS_MAGAZINE, true);
                LOGGER.info("Successfully set GUN_MODEL_TYPE to 1");
            } catch (Exception e) {
                LOGGER.error("Failed to set GUN_MODEL_TYPE: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 无弹匣时移除 GUN_MODEL_TYPE 或设置为 0，显示 ak47_empty 模型
            LOGGER.info("Removing GUN_MODEL_TYPE for gun without magazine");
            try {
                stack.remove(HAS_MAGAZINE);
                LOGGER.info("Successfully removed GUN_MODEL_TYPE");
            } catch (Exception e) {
                LOGGER.error("Failed to remove GUN_MODEL_TYPE: {}", e.getMessage());
                e.printStackTrace();
            }
        }

        // 检查设置后的状态
        var hasModelType = stack.has(HAS_MAGAZINE);
        LOGGER.info("Final state - Has GUN_MODEL_TYPE component: {}", hasModelType);

        if (hasModelType) {
            var modelType = stack.get(HAS_MAGAZINE);
            LOGGER.info("GUN_MODEL_TYPE value: {}", modelType);
        }

        LOGGER.info("Updated reload state for gun: isReloading={}, hasMagazine={}", isReloading, hasMagazine);
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
            ServerPlayNetworking.send(player, payload);
            LOGGER.info("Sent item component sync packet to client - hasMagazine: {}", hasMagazine);
        }
    }


    public TagKey<Item> compatibleMagazines() {
        return this.compatibleMagazines;
    }
}
