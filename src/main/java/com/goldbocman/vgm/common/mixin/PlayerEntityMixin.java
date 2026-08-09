package com.goldbocman.vgm.common.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.events.MEvents;
import com.goldbocman.vgm.common.network.NetworkUtils;
import com.goldbocman.vgm.common.registry.AttributeReg;
import com.goldbocman.vgm.common.registry.NetworkReg;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.registry.custom.types.GunProperties;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;
import com.goldbocman.vgm.common.utils.GunUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IGunUser {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/PlayerEntityMixin");
    @Shadow public abstract ItemCooldowns getCooldowns();

    @Unique
    private boolean canReload = true;
    @Unique
    private ItemStack reloadingGun = ItemStack.EMPTY;
    @Unique
    private ItemStack lastGun = ItemStack.EMPTY;
    @Unique
    private int shootingDur = 0;
    @Unique
    private int gunTicks = 0;
    @Unique
    private GunHelper.GunStates gunState = GunHelper.GunStates.NORMAL;

    // 新增瞄准相关字段
    @Unique
    private boolean isAiming = false;
    @Unique
    private float aimProgress = 0.0f;
    @Unique
    private static final float AIM_SPEED = 0.1f;
    @Unique
    private static final float MAX_AIM_PROGRESS = 1.0f;

    public PlayerEntityMixin(Level world, BlockPos ignoredPos, float ignoredYaw, GameProfile ignoredGameProfile) {
        //? if >=1.21.11 {
        /*super((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(com.goldbocman.vgm.TemplateModCommon.id("minecraft", "player")).orElseThrow().value(), world);
        *///?} else {
        // Registry.get(ResourceLocation) returns T directly pre-1.21.11, not Optional<Holder<T>>.
        super((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(com.goldbocman.vgm.TemplateModCommon.id("minecraft", "player")), world);
        //?}
    }

    // 新增瞄准相关方法实现
    @Override
    public boolean bren_1_21_1$isAiming() {
        return this.isAiming;
    }

    @Override
    public void bren_1_21_1$setAiming(boolean aiming) {
        this.isAiming = aiming;
    }

    @Override
    public float bren_1_21_1$getAimProgress() {
        return this.aimProgress;
    }

    @Override
    public void bren_1_21_1$setAimProgress(float progress) {
        this.aimProgress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    @Inject(method = "isScoping",
            //? if forge
            remap = false,
            at = @At("HEAD"), cancellable = true)
    public void blackPowder$gunScopes(CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack = this.getMainHandItem();
        if (itemStack.getItem() instanceof GunItem gunItem) {
            if (gunItem.supportsAiming() && this.bren_1_21_1$isAiming() && this.bren_1_21_1$getAimProgress() > 0.7f) {
                // 直接使用原版望远镜逻辑，当瞄准进度足够高时返回true
                cir.setReturnValue(true);
            }
        }
    }


    @Unique
    private void updateAimingState() {
        Player player = (Player) (Object) this;
        ItemStack mainHandStack = player.getMainHandItem();

        // 检查是否手持Rifle并下蹲
        boolean shouldAim = !mainHandStack.isEmpty() &&
                mainHandStack.getItem() instanceof GunItem &&
                isRifle(mainHandStack) &&
                player.isShiftKeyDown();

        if (shouldAim != this.isAiming) {
            this.isAiming = shouldAim;
        }

        // 更新瞄准进度
        if (this.isAiming) {
            this.aimProgress = Math.min(this.aimProgress + AIM_SPEED, MAX_AIM_PROGRESS);
        } else {
            this.aimProgress = Math.max(this.aimProgress - AIM_SPEED, 0.0f);
        }

        // 当瞄准进度足够高时，调用望远镜mixin功能
        // 注释掉此行以避免与自定义FOV冲突
        // if (this.aimProgress > 0.7f && this.isAiming) {
        //     activateSpyglassZoom(player);
        // }
    }

    @Unique
    private boolean isRifle(ItemStack stack) {
        // 检查是否为Rifle类型的枪械
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId != null && itemId.getPath().toLowerCase().contains("rifle");
    }

    @Unique
    private void activateSpyglassZoom(Player player) {
        // 这里调用原版望远镜的缩放功能
        // 在Minecraft 1.21.4中，望远镜功能通过SpyglassItem实现
        if (player.level().isClientSide()) {
            // 客户端调用望远镜缩放效果
            applySpyglassZoomEffect();
        }
    }

    @Unique
    private void applySpyglassZoomEffect() {
        // Genuinely unimplemented (no FOV-scaling logic ever existed here) - previously had a dead
        // `Minecraft.getInstance() != null` check with an empty body, which was a real bug: this is a
        // common-loaded mixin (applied on both client and server), and Forge's RuntimeDistCleaner
        // fails class-loading/mixin-preprocessing on a dedicated server the moment ANY common class
        // references net.minecraft.client.Minecraft, even behind a runtime isClientSide() guard and
        // even when the reference does nothing - see claude/stonecutter-multiloader.md's
        // "common-loaded mixin must never directly reference a client-only class" note (first found
        // during the NeoForge port, hit again here for Forge's dedicated server specifically).
    }

    @Override
    public boolean bren_1_21_1$isShooting() {
        Player player = (Player) (Object) this;
        return player.isUsingItem() && player.getUseItem().getItem() instanceof GunItem;
    }

    @Override
    public int bren_1_21_1$shootingDuration() {
        return this.shootingDur;
    }

    @Override
    public boolean bren_1_21_1$canShoot(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandItem());
    }

    @Override
    public void bren_1_21_1$setReloadingGun(ItemStack reloadingGun) {
        this.reloadingGun = reloadingGun;
    }

    @Unique
    public void handleShooting$bren$() {

        ItemCooldowns c = this.getCooldowns();
        ItemStack mainHandStack = this.getMainHandItem();

        // 修复：简化射击条件判断逻辑
        if (mainHandStack.isEmpty() || !(mainHandStack.getItem() instanceof GunItem)) {
            return;
        }

        // 关键修复：添加右键检测，只有按下右键时才射击
        if (!this.bren_1_21_1$isShooting()) {
            return;
        }

        // 修复：检查冷却时间（使用ItemStack而不是Item）
        if (GunApiCompat.isOnCooldown(c, mainHandStack)) {
            return;
        }

        // 关键修复：简化弹药检查，确保只要弹药不为零就可以开火
        GunItem gunItem = (GunItem) mainHandStack.getItem();
        if (gunItem.isEmpty(mainHandStack)) {
            return;
        }

        // 关键修复：移除对枪械状态的检查，确保只要弹药不为零就可以开火
        // if (!this.bren_1_21_1$getGunState().equals(GunHelper.GunStates.NORMAL)) {
        //     return;
        // }

        // 关键修复：直接执行射击逻辑
        this.bren_1_21_1$setGunTicks(16);

        int fireRate = GunUtils.fire(this);
        if (fireRate == 0) {
            return;
        }

        GunApiCompat.addCooldown(c, mainHandStack, fireRate);

        Player player = (Player) (Object) this;

        // 新增：客户端发送射击数据包到服务器
        if (player.level().isClientSide()) {
            Vec3 origin = player.getEyePosition();
            Vec3 direction = player.getViewVector(1.0F);

            // 创建射击数据包并发送到服务器
            NetworkReg.ShootPayload payload = new NetworkReg.ShootPayload(
                    (float) origin.x, (float) origin.y, (float) origin.z,
                    (float) direction.x, (float) direction.y, (float) direction.z,
                    true // 是否抛出弹壳
            );
            NetworkUtils.sendToServer(payload);
        }

        MEvents.GUN_FIRED_EVENT.invoker().gunFired(player, mainHandStack);

        // 关键修复：移除可能导致原版物品挥动动画的调用
        // GunUtils.sendAnimationPacket(player);
    }

    @Inject(at = @At("RETURN"), method = "createAttributes"
            //? if forge
            , remap = false
    )
    private static void createPlayerAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        var builder = cir.getReturnValue();

        // 修复：使用RegistryEntry包装EntityAttribute
        // AttributeSupplier.Builder.add takes a plain Attribute pre-1.20.5, not a Holder<Attribute> -
        // confirmed via javap on the real 1.20.1 merged jar.
        if (AttributeReg.RANGED_DAMAGE != null) {
            //? if >=1.20.5 {
            /*builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeReg.RANGED_DAMAGE), 5d); // 默认伤害值
            *///?} else {
            builder.add(AttributeReg.RANGED_DAMAGE, 5d); // 默认伤害值
            //?}
        }
        if (AttributeReg.FIRE_RATE != null) {
            //? if >=1.20.5 {
            /*builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeReg.FIRE_RATE), 3d); // 默认射速
            *///?} else {
            builder.add(AttributeReg.FIRE_RATE, 3d); // 默认射速
            //?}
        }
        if (AttributeReg.RECOIL != null) {
            //? if >=1.20.5 {
            /*builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeReg.RECOIL), 1d); // 默认后坐力
            *///?} else {
            builder.add(AttributeReg.RECOIL, 1d); // 默认后坐力
            //?}
        }
    }

    @Override
    public boolean bren_1_21_1$canReload() {
        return this.canReload;
    }

    @Override
    public void bren_1_21_1$setCanReload(boolean canReload) {
        this.canReload = canReload;
    }

    @Override
    public ItemStack bren_1_21_1$getLastGun() {
        return this.lastGun;
    }

    @Override
    public int bren_1_21_1$getGunTicks() {
        return this.gunTicks; // 修改：返回实例字段值
    }

    @Override
    public void bren_1_21_1$setGunTicks(int ticks) {
        this.gunTicks = Math.max(ticks, 0); // 修改：设置实例字段值
    }

    @Override
    public GunHelper.GunStates bren_1_21_1$getGunState() {
        return this.gunState; // 修改：返回实例字段值
    }

    @Override
    public void bren_1_21_1$setGunState(GunHelper.GunStates state) {
        this.gunState = state; // 修改：设置实例字段值
        // Sync to the owning player's own client - state only ever changes server-side, and
        // NetworkUtils.sendToPlayer no-ops unless `this` is actually a ServerPlayer, so this is
        // safe to call unconditionally from every call site (including client-side ones, if any).
        NetworkUtils.sendToPlayer((Player) (Object) this, new NetworkReg.GunStatePayload(state.getId()));
    }

    @Override
    public ItemStack bren_1_21_1$getReloadingGun() {
        return this.reloadingGun;
    }



    // 删除重复的getGunState和setGunState方法定义（第185-190行）

    @Inject(at = @At("TAIL"), method = "tick"
            //? if forge
            , remap = false
    )
    public void tickMovement(CallbackInfo ci) {
        this.bren_1_21_1$setGunTicks(this.bren_1_21_1$getGunTicks() - 1);
    }

    // 删除这个重复的dataTracker方法定义（第216-221行）

    @Inject(method = "tick",
            //? if forge
            remap = false,
            at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // 修复：重新添加属性应用逻辑
        this.applyGunAttributes();
    }

    @Unique
    private void applyGunAttributes() {
        Player player = (Player) (Object) this;
        ItemStack mainHandStack = player.getMainHandItem();

        if (mainHandStack.isEmpty() || !(mainHandStack.getItem() instanceof GunItem gunItem)) {
            // 如果没有持枪，重置属性为默认值
            this.resetGunAttributes();
            return;
        }

        // 获取枪械属性
        GunProperties properties = gunItem.getGunProperties(mainHandStack);
        if (properties == null) {
            this.resetGunAttributes();
            return;
        }

        // 应用枪械属性到玩家（注意类型转换）
        this.applyAttribute(AttributeReg.RANGED_DAMAGE, (double) properties.rangedDamage);
        this.applyAttribute(AttributeReg.FIRE_RATE, (double) properties.fireRate);
        this.applyAttribute(AttributeReg.RECOIL, (double) properties.recoil);
    }

    @Unique
    private void resetGunAttributes() {
        // 重置属性为默认值
        this.applyAttribute(AttributeReg.RANGED_DAMAGE, 0d);
        this.applyAttribute(AttributeReg.FIRE_RATE, 0d);
        this.applyAttribute(AttributeReg.RECOIL, 0d);
    }

    @Unique
    private void applyAttribute(Attribute attribute, double value) {
        if (attribute == null) return;

        Player player = (Player) (Object) this;

        // 修复：使用RegistryEntry包装EntityAttribute进行检查和操作
        // LivingEntity.getAttribute takes a plain Attribute pre-1.20.5, not a Holder<Attribute> -
        // confirmed via javap on the real 1.20.1 merged jar.
        //? if >=1.20.5 {
        /*var attributeEntry = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
        *///?} else {
        var attributeEntry = attribute;
        //?}
        if (attributeEntry != null) {
            // 获取属性实例并设置值
            var attributeInstance = player.getAttribute(attributeEntry);
            if (attributeInstance != null) {
                // 清除所有临时修改器，然后应用新的值
                attributeInstance.removeModifiers();
                attributeInstance.setBaseValue(value);
            }
            // 移除警告日志，因为GunUtils.fire方法已经直接使用GunItem属性
            // 属性实例可能不存在是正常的，因为GunUtils.fire不依赖属性实例
        } else {
            LOGGER.warn("Attribute entry not found for attribute: {}", attribute);
        }
    }

    @Inject(method = "tick",
            //? if forge
            remap = false,
            at = @At("TAIL"))
    private void reloadTick$bren$(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        IGunUser gunUser = (IGunUser) player;

        // 关键修复：添加射击处理调用
        this.handleShooting$bren$();

        // 新增：更新瞄准状态
        this.updateAimingState();

        ItemStack reloadingGun = gunUser.bren_1_21_1$getReloadingGun();

        // reloadTick() physically moves/creates items (unloadMagazine/putMagazine, bullet insertion) -
        // it must run server-authoritative only. This mixin targets the base Player class, so it also
        // fires on the client's own LocalPlayer every tick; gunState now syncs to the client (see
        // setGunState below), which lets bren$inventoryTick populate the CLIENT's own reloadingGun
        // field too. Since the client's ItemCooldowns for the gun was never actually put on cooldown
        // (addCooldown only happens inside the server-only-invoked onReload()), an ungated call here
        // let the client complete the "reload" near-instantly on its own predicted inventory, racing
        // ahead of the server's real ~20-tick completion - the two independent runs of
        // unloadMagazine/putMagazine (one per side) is what produced the duplicated magazine.
        if (!player.level().isClientSide() && !reloadingGun.isEmpty() && reloadingGun.getItem() instanceof GunItem gunItem) {
            // Magazine-swap reloads (GunWithMagItem: rifle/auto_gun/flare gun/machine gun) must cancel
            // if the player switches active slot away from the reloading gun - otherwise it kept
            // completing silently in the background even though it was no longer held. Reference
            // equality is safe here: reloadingGun is the literal ItemStack instance captured by
            // onReload(), and switching hotbar slots doesn't replace inventory slot objects, only which
            // one is selected. BulletOnlyGun (revolver/shotgun/...) is intentionally excluded, matching
            // NetworkReg.handleCancelReload's release-cancel exclusion - those reloads are meant to be
            // spam-clickable and finish regardless of what's currently held.
            if (gunItem instanceof GunWithMagItem
                    && reloadingGun != player.getMainHandItem() && reloadingGun != player.getOffhandItem()) {
                GunApiCompat.removeCooldown(player.getCooldowns(), reloadingGun);
                gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
                gunUser.bren_1_21_1$setCanReload(true);
                gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
            } else {
                gunItem.reloadTick(reloadingGun, player.level(), player, gunUser);
            }
        }

        // 更新弹匣相关的组件
        updateMagazineComponents(player);
    }

    // 更新弹匣相关的组件
    @Unique
    private void updateMagazineComponents(Player player) {
        // 检查主手物品
        ItemStack mainHandStack = player.getMainHandItem();
        if (mainHandStack.getItem() instanceof com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem) {
            updateGunMagazineComponents(mainHandStack);
        }

        // 检查副手物品
        ItemStack offHandStack = player.getOffhandItem();
        if (offHandStack.getItem() instanceof com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem) {
            updateGunMagazineComponents(offHandStack);
        }
    }

    // 更新枪械的弹匣组件
    @Unique
    private void updateGunMagazineComponents(ItemStack gunStack) {
        if (!(gunStack.getItem() instanceof com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem)) {
            return;
        }

        // 检查是否有弹匣
        boolean hasMagazine = com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem.hasMagazine(gunStack);

        // 检查弹匣是否是可染色的
        boolean hasColorableMagazine = false;
        if (hasMagazine) {
            var nbt = GunApiCompat.getTag(gunStack);
            String magItemId = GunApiCompat.getString(nbt, "MagazineItem");

            if (!magItemId.isEmpty()) {
                var itemId = ResourceLocation.tryParse(magItemId);
                if (itemId != null) {
                    var item = GunApiCompat.getItem(itemId);
                    if (item instanceof com.goldbocman.vgm.common.registry.custom.ColorableMagazineItem) {
                        hasColorableMagazine = true;
                    }
                }
            }
        }

        // 更新弹匣相关的NBT数据
        var nbt = GunApiCompat.getTag(gunStack);

        // 更新弹匣状态信息
        nbt.putBoolean("bren_has_magazine", hasMagazine);
        nbt.putBoolean("bren_has_colorable_magazine", hasColorableMagazine);

        // 设置更新后的自定义数据
        GunApiCompat.setTag(gunStack, nbt);
    }
}