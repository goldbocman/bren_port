package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.TagReg;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;
import nl.sniffiandros.bren.common.registry.custom.PoseType;
import nl.sniffiandros.bren.common.utils.GunHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlareGunItem extends GunWithMagItem  {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/FlareGunItem");
    private static final String HAS_MAGAZINE_KEY = "HasMagazine";
    private static final String MAGAZINE_CAPACITY_KEY = "MagazineCapacity";
    private static final String MAGAZINE_CONTENTS_KEY = "MagazineContents";
    private static final String MAGAZINE_ITEM_KEY = "MagazineItem";


    // 修改构造函数，接收Item.Settings参数
    public FlareGunItem(Properties settings, TagKey<Item> shortMagazines) {
        super(settings, TagReg.SHORT_MAGAZINES);
    }

    @Override
    public int reloadSpeed() {
        return 6;
    }

    @Override
    public int bulletAmount() {
        return super.bulletAmount();
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
    @Override
    public int getMaxCapacity(ItemStack stack) {
        // 为霰弹枪设置合适的容量，通常霰弹枪有6-8发容量
        // 这里设置为8发，可以根据实际需求调整
        return 15; // 修复：直接返回int值，而不是Optional.of(8)
    }

    @Override
    public PoseType holdingPose() {
        return PoseType.REVOLVER;
    }

}