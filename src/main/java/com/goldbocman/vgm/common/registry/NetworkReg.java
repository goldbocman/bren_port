package com.goldbocman.vgm.common.registry;

//? if fabric {
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?}
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?}
//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.MagazineItem;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;
import com.goldbocman.vgm.common.utils.GunUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkReg {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/NetworkReg");

    // CustomPacketPayload.Type doesn't exist before 1.20.5 - old Fabric networking (and the loaders
    // that only ever targeted <1.20.5) identify channels by a plain Identifier instead. Every
    // ID constant below keeps the same name across both branches so every call site elsewhere in the
    // mod (NeoForgePayloads, ForgeNetwork, ClientNetworkReg, this file's own registerAllPackets())
    // compiles unchanged regardless of version - only the constant's own type differs.
    //? if >=1.20.5 {
    public static final CustomPacketPayload.Type<ReloadPayload> RELOAD_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "reload"));
    public static final CustomPacketPayload.Type<CancelReloadPayload> CANCEL_RELOAD_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "cancel_reload"));
    public static final CustomPacketPayload.Type<RecoilPayload> RECOIL_CLIENT_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "recoil_client"));
    public static final CustomPacketPayload.Type<ShootClientPayload> SHOOT_CLIENT_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot_client"));
    public static final CustomPacketPayload.Type<ShootPayload> SHOOT_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot"));
    public static final CustomPacketPayload.Type<ShootAnimationPayload> SHOOT_ANIMATION_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot_animation"));
    public static final CustomPacketPayload.Type<ItemComponentSyncPayload> ITEM_COMPONENT_SYNC_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "item_component_sync"));
    public static final CustomPacketPayload.Type<GunStatePayload> GUN_STATE_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "gun_state"));
    public static final CustomPacketPayload.Type<ShootParticlePayload> SHOOT_PARTICLE_PACKET_ID = new CustomPacketPayload.Type<>(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot_particle"));
    //?} else {
    /*// Identifier.fromNamespaceAndPath doesn't exist before 1.21 either (confirmed via javap -
    // only the old two-arg constructor does) - TemplateModCommon.id(...) already bridges exactly
    // this gap.
    public static final Identifier RELOAD_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "reload");
    public static final Identifier CANCEL_RELOAD_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "cancel_reload");
    public static final Identifier RECOIL_CLIENT_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "recoil_client");
    public static final Identifier SHOOT_CLIENT_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot_client");
    public static final Identifier SHOOT_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot");
    public static final Identifier SHOOT_ANIMATION_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot_animation");
    public static final Identifier ITEM_COMPONENT_SYNC_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "item_component_sync");
    public static final Identifier GUN_STATE_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "gun_state");
    public static final Identifier SHOOT_PARTICLE_PACKET_ID = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "shoot_particle");
    *///?}

    private static void handleReload(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();

        if (server != null) {
            server.execute(() -> {
                ItemStack stack = player.getMainHandItem();

                if (stack.getItem() instanceof GunItem gunItem) {
                    gunItem.onReload(player);
                } else if (stack.getItem() instanceof MagazineItem) {
                    GunUtils.fillMagazine(stack, player);
                }
            });
        }
    }

    // Aborts an in-progress reload (hold-R-to-reload, release-early-to-cancel - see
    // KeyBindingReg.consumeReloadClicks). Runs entirely upstream of any gun-specific reloadTick()
    // completion logic, so it only needs to unwind the shared IGunUser state - no per-gun-type
    // dispatch needed beyond the BulletOnlyGun exclusion below. Guarding on gunState==RELOADING makes
    // this self-safe: if the cancel packet arrives after the reload already finished naturally
    // (network latency), it's a no-op. Uses the tracked reloadingGun field rather than the player's
    // current mainhand item so this stays correct even if the player already swapped away from the
    // reloading gun before releasing R.
    private static void handleCancelReload(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();

        if (server != null) {
            server.execute(() -> {
                IGunUser gunUser = (IGunUser) player;
                if (gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.RELOADING)) {
                    ItemStack reloadingGun = gunUser.bren_1_21_1$getReloadingGun();
                    // BulletOnlyGun (revolver/shotgun/air gun/...) reloads are meant to be
                    // spam-clickable - a single bullet insertion gated only by the existing cooldown,
                    // not cancellable. Only magazine-swap guns (GunWithMagItem: rifle/auto_gun/flare
                    // gun/machine gun), whose reload takes a full ~20 ticks, honor cancel-on-release.
                    if (reloadingGun.isEmpty() || !(reloadingGun.getItem() instanceof GunWithMagItem)) {
                        return;
                    }
                    GunApiCompat.removeCooldown(player.getCooldowns(), reloadingGun);
                    gunUser.bren_1_21_1$setGunState(GunHelper.GunStates.NORMAL);
                    gunUser.bren_1_21_1$setCanReload(true);
                    gunUser.bren_1_21_1$setReloadingGun(ItemStack.EMPTY);
                }
            });
        }
    }

    private static void handleShoot(ServerPlayer player) {
        // 获取玩家和主手物品
        // 检查是否为GunItem
        // 调用GunUtils.fire方法执行射击逻辑
        // 设置冷却时间
        // 发送粒子效果到客户端
        // 触发射击事件
    }

    //? if fabric {
    //? if >=1.20.5 {
    public static void registerAllPackets() {
        LOGGER.info("Registering all network packets");

        // 注册所有数据包类型
        //? if >=1.21.11 {
        // 客户端接收的数据包（S2C - Server to Client）
        PayloadTypeRegistry.clientboundPlay().register(RECOIL_CLIENT_PACKET_ID, RecoilPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_CLIENT_PACKET_ID, ShootClientPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_ANIMATION_PACKET_ID, ShootAnimationPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_PARTICLE_PACKET_ID, ShootParticlePayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ITEM_COMPONENT_SYNC_PACKET_ID, ItemComponentSyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GUN_STATE_PACKET_ID, GunStatePayload.PACKET_CODEC);
        // 服务器端接收的数据包（C2S - Client to Server）
        PayloadTypeRegistry.serverboundPlay().register(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CANCEL_RELOAD_PACKET_ID, CancelReloadPayload.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SHOOT_PACKET_ID, ShootPayload.PACKET_CODEC);
        //?} else {
        /*// Pre-1.21.11 fabric-networking-api-v1 names these playS2C()/playC2S() instead of the newer
        // clientboundPlay()/serverboundPlay().
        PayloadTypeRegistry.playS2C().register(RECOIL_CLIENT_PACKET_ID, RecoilPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SHOOT_CLIENT_PACKET_ID, ShootClientPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SHOOT_ANIMATION_PACKET_ID, ShootAnimationPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SHOOT_PARTICLE_PACKET_ID, ShootParticlePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ITEM_COMPONENT_SYNC_PACKET_ID, ItemComponentSyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(GUN_STATE_PACKET_ID, GunStatePayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CANCEL_RELOAD_PACKET_ID, CancelReloadPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SHOOT_PACKET_ID, ShootPayload.PACKET_CODEC);
        *///?}

        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, (payload, context) -> handleReload(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(CANCEL_RELOAD_PACKET_ID, (payload, context) -> handleCancelReload(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (payload, context) -> handleShoot(context.player()));

        LOGGER.info("All network packets registered successfully");
    }
    //?} else {
    /*// 1.20.1 predates CustomPacketPayload/StreamCodec/the payload-type-registry system entirely -
    // channels are identified by a bare Identifier and receivers get the raw FriendlyByteBuf
    // directly (confirmed via javap against the real fabric-networking-api-v1 1.3.15+0caff8d077 jar,
    // the exact submodule version fabric-api 0.92.11+1.20.1 pins). Reuses the same
    // handleReload/handleShoot methods as every other version - only the registration mechanics differ.
    public static void registerAllPackets() {
        LOGGER.info("Registering all network packets (legacy pre-1.20.5 networking)");

        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, (server, player, handler, buf, sender) -> handleReload(player));
        ServerPlayNetworking.registerGlobalReceiver(CANCEL_RELOAD_PACKET_ID, (server, player, handler, buf, sender) -> handleCancelReload(player));
        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (server, player, handler, buf, sender) -> handleShoot(player));

        LOGGER.info("All network packets registered successfully");
    }
    *///?}
    //?}
    //? if neoforge
    //public static void registerAllPackets() {} // NeoForge registers via NeoForgePayloads (RegisterPayloadHandlersEvent) below instead.
    //? if forge
    //public static void registerAllPackets() {} // Forge registers via ForgeNetwork's static CHANNEL field below instead.

    // Every payload record below keeps the same name/fields/constructor across both branches (so the
    // ~7 call sites elsewhere in the mod that do `new NetworkReg.XPayload(...)` compile unchanged
    // regardless of version) and keeps read(buf)/write(buf) methods with the same names too (so
    // registerAllPackets()/NetworkUtils's send helpers can call them uniformly) - only the buffer
    // type (RegistryFriendlyByteBuf vs plain FriendlyByteBuf, the latter being all that exists
    // pre-1.20.5) and whether the record implements CustomPacketPayload differ.

    // 重装包数据类
    //? if >=1.20.5 {
    public record ReloadPayload() implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ReloadPayload> PACKET_CODEC = StreamCodec.unit(new ReloadPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return RELOAD_PACKET_ID;
        }
    }
    //?} else {
    /*public record ReloadPayload() {
        public static ReloadPayload read(FriendlyByteBuf buf) {
            return new ReloadPayload();
        }

        public void write(FriendlyByteBuf buf) {
        }
    }
    *///?}

    // Sent when the reload key is released before the reload cooldown finishes - see
    // KeyBindingReg.consumeReloadClicks / NetworkReg.handleCancelReload.
    //? if >=1.20.5 {
    public record CancelReloadPayload() implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, CancelReloadPayload> PACKET_CODEC = StreamCodec.unit(new CancelReloadPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return CANCEL_RELOAD_PACKET_ID;
        }
    }
    //?} else {
    /*public record CancelReloadPayload() {
        public static CancelReloadPayload read(FriendlyByteBuf buf) {
            return new CancelReloadPayload();
        }

        public void write(FriendlyByteBuf buf) {
        }
    }
    *///?}

    // 后坐力数据包
    //? if >=1.20.5 {
    public record RecoilPayload(float recoil) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, RecoilPayload> PACKET_CODEC = StreamCodec.ofMember(
            RecoilPayload::write,
            RecoilPayload::read
        );

        public static RecoilPayload read(RegistryFriendlyByteBuf buf) {
            return new RecoilPayload(buf.readFloat());
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeFloat(recoil);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return RECOIL_CLIENT_PACKET_ID;
        }
    }
    //?} else {
    /*public record RecoilPayload(float recoil) {
        public static RecoilPayload read(FriendlyByteBuf buf) {
            return new RecoilPayload(buf.readFloat());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeFloat(recoil);
        }
    }
    *///?}

    // Syncs the owning player's GunHelper.GunStates (NORMAL/RELOADING) to their own client - the
    // state itself only ever changes server-side (onReload/reloadTick), and without this the
    // client's IGunUser copy never leaves NORMAL, so applyCustomMatrix's reload-specific animation
    // branch was permanently dead code.
    //? if >=1.20.5 {
    public record GunStatePayload(int state) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, GunStatePayload> PACKET_CODEC = StreamCodec.ofMember(
            GunStatePayload::write,
            GunStatePayload::read
        );

        public static GunStatePayload read(RegistryFriendlyByteBuf buf) {
            return new GunStatePayload(buf.readVarInt());
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(state);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return GUN_STATE_PACKET_ID;
        }
    }
    //?} else {
    /*public record GunStatePayload(int state) {
        public static GunStatePayload read(FriendlyByteBuf buf) {
            return new GunStatePayload(buf.readVarInt());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(state);
        }
    }
    *///?}

    // 客户端射击声音数据包
    //? if >=1.20.5 {
    public record ShootClientPayload(float volume) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ShootClientPayload> PACKET_CODEC = StreamCodec.ofMember(
            ShootClientPayload::write,
            ShootClientPayload::read
        );

        public static ShootClientPayload read(RegistryFriendlyByteBuf buf) {
            return new ShootClientPayload(buf.readFloat());
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeFloat(volume);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return SHOOT_CLIENT_PACKET_ID;
        }
    }
    //?} else {
    /*public record ShootClientPayload(float volume) {
        public static ShootClientPayload read(FriendlyByteBuf buf) {
            return new ShootClientPayload(buf.readFloat());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeFloat(volume);
        }
    }
    *///?}

    // 射击动画数据包（无数据）
    //? if >=1.20.5 {
    public record ShootAnimationPayload() implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ShootAnimationPayload> PACKET_CODEC = StreamCodec.unit(new ShootAnimationPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return SHOOT_ANIMATION_PACKET_ID;
        }
    }
    //?} else {
    /*public record ShootAnimationPayload() {
        public static ShootAnimationPayload read(FriendlyByteBuf buf) {
            return new ShootAnimationPayload();
        }

        public void write(FriendlyByteBuf buf) {
        }
    }
    *///?}

    // 射击粒子效果数据包
    //? if >=1.20.5 {
    public record ShootPayload(float originX, float originY, float originZ,
                              float directionX, float directionY, float directionZ,
                              boolean casing) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ShootPayload> PACKET_CODEC = StreamCodec.ofMember(
            ShootPayload::write,
            ShootPayload::read
        );

        public static ShootPayload read(RegistryFriendlyByteBuf buf) {
            return new ShootPayload(
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean()
            );
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeFloat(originX);
            buf.writeFloat(originY);
            buf.writeFloat(originZ);
            buf.writeFloat(directionX);
            buf.writeFloat(directionY);
            buf.writeFloat(directionZ);
            buf.writeBoolean(casing);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return SHOOT_PACKET_ID;
        }
    }
    //?} else {
    /*public record ShootPayload(float originX, float originY, float originZ,
                              float directionX, float directionY, float directionZ,
                              boolean casing) {
        public static ShootPayload read(FriendlyByteBuf buf) {
            return new ShootPayload(
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean()
            );
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeFloat(originX);
            buf.writeFloat(originY);
            buf.writeFloat(originZ);
            buf.writeFloat(directionX);
            buf.writeFloat(directionY);
            buf.writeFloat(directionZ);
            buf.writeBoolean(casing);
        }
    }
    *///?}

    // 射击粒子效果数据包（S2C）
    //? if >=1.20.5 {
    public record ShootParticlePayload(float originX, float originY, float originZ,
                                      float directionX, float directionY, float directionZ,
                                      boolean casing) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ShootParticlePayload> PACKET_CODEC = StreamCodec.ofMember(
            ShootParticlePayload::write,
            ShootParticlePayload::read
        );

        public static ShootParticlePayload read(RegistryFriendlyByteBuf buf) {
            return new ShootParticlePayload(
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean()
            );
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeFloat(originX);
            buf.writeFloat(originY);
            buf.writeFloat(originZ);
            buf.writeFloat(directionX);
            buf.writeFloat(directionY);
            buf.writeFloat(directionZ);
            buf.writeBoolean(casing);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return SHOOT_PARTICLE_PACKET_ID;
        }
    }
    //?} else {
    /*public record ShootParticlePayload(float originX, float originY, float originZ,
                                      float directionX, float directionY, float directionZ,
                                      boolean casing) {
        public static ShootParticlePayload read(FriendlyByteBuf buf) {
            return new ShootParticlePayload(
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean()
            );
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeFloat(originX);
            buf.writeFloat(originY);
            buf.writeFloat(originZ);
            buf.writeFloat(directionX);
            buf.writeFloat(directionY);
            buf.writeFloat(directionZ);
            buf.writeBoolean(casing);
        }
    }
    *///?}

    //? if >=1.20.5 {
    public record ItemComponentSyncPayload(int slot, boolean hasMagazine) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemComponentSyncPayload> PACKET_CODEC = StreamCodec.ofMember(
                ItemComponentSyncPayload::write,
                ItemComponentSyncPayload::read
        );

        public static ItemComponentSyncPayload read(RegistryFriendlyByteBuf buf) {
            int slot = buf.readInt();
            boolean hasMagazine = buf.readBoolean();
            return new ItemComponentSyncPayload(slot, hasMagazine);
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeInt(slot);
            buf.writeBoolean(hasMagazine);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ITEM_COMPONENT_SYNC_PACKET_ID;
        }
    }
    //?} else {
    /*public record ItemComponentSyncPayload(int slot, boolean hasMagazine) {
        public static ItemComponentSyncPayload read(FriendlyByteBuf buf) {
            int slot = buf.readInt();
            boolean hasMagazine = buf.readBoolean();
            return new ItemComponentSyncPayload(slot, hasMagazine);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(slot);
            buf.writeBoolean(hasMagazine);
        }
    }
    *///?}

    //? if neoforge {
    /*//? if >=1.21.9 {
    /^@net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID)
    ^///?} else {
    @net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
    //?}
    public static class NeoForgePayloads {
        @net.neoforged.bus.api.SubscribeEvent
        public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
            // "1" is the payload-version string NeoForge uses to reject clients/servers with mismatched
            // channel versions - bump it if any of these payload records ever change shape.
            net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar("1");

            // 服务器端接收的数据包（C2S - Client to Server）
            registrar.playToServer(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC,
                    (payload, context) -> handleReload((net.minecraft.server.level.ServerPlayer) context.player()));
            registrar.playToServer(CANCEL_RELOAD_PACKET_ID, CancelReloadPayload.PACKET_CODEC,
                    (payload, context) -> handleCancelReload((net.minecraft.server.level.ServerPlayer) context.player()));
            registrar.playToServer(SHOOT_PACKET_ID, ShootPayload.PACKET_CODEC,
                    (payload, context) -> handleShoot((net.minecraft.server.level.ServerPlayer) context.player()));

            // 客户端接收的数据包（S2C - Server to Client）
            registrar.playToClient(RECOIL_CLIENT_PACKET_ID, RecoilPayload.PACKET_CODEC,
                    (payload, context) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleRecoil(net.minecraft.client.Minecraft.getInstance(), payload));
            registrar.playToClient(SHOOT_CLIENT_PACKET_ID, ShootClientPayload.PACKET_CODEC,
                    (payload, context) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleClientShoot(net.minecraft.client.Minecraft.getInstance(), payload));
            registrar.playToClient(SHOOT_ANIMATION_PACKET_ID, ShootAnimationPayload.PACKET_CODEC,
                    (payload, context) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleShootAnimation(net.minecraft.client.Minecraft.getInstance()));
            registrar.playToClient(SHOOT_PARTICLE_PACKET_ID, ShootParticlePayload.PACKET_CODEC,
                    (payload, context) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleShootParticle(net.minecraft.client.Minecraft.getInstance(), payload));
            registrar.playToClient(ITEM_COMPONENT_SYNC_PACKET_ID, ItemComponentSyncPayload.PACKET_CODEC,
                    (payload, context) -> {});
            registrar.playToClient(GUN_STATE_PACKET_ID, GunStatePayload.PACKET_CODEC,
                    (payload, context) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleGunState(net.minecraft.client.Minecraft.getInstance(), payload));
        }
    }
    *///?}

    // Forge 1.21.1 (eventbus 6.2.27) has no NeoForge-style RegisterPayloadHandlersEvent/
    // PayloadRegistrar - confirmed via javap against the real forge-1.21.1-52.1.0 sources jar, which
    // has no such class anywhere under net.minecraftforge.network. It kept its own classic
    // ChannelBuilder/SimpleChannel-family API instead, adapted to accept vanilla's
    // CustomPacketPayload/StreamCodec records via ChannelBuilder.payloadChannel() - a plain static
    // field built once at class-load time (triggered by TemplateModForge's constructor referencing
    // this class), not an event-driven registration.
    //? if forge {
    /*//? if >=1.20.5 {
    public static class ForgeNetwork {
        public static final net.minecraftforge.network.Channel<CustomPacketPayload> CHANNEL = net.minecraftforge.network.ChannelBuilder
                .named(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "main"))
                .networkProtocolVersion(1)
                .payloadChannel()
                .play()
                .serverbound()
                    .addMain(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC, (payload, ctx) -> handleReload(ctx.getSender()))
                    .addMain(CANCEL_RELOAD_PACKET_ID, CancelReloadPayload.PACKET_CODEC, (payload, ctx) -> handleCancelReload(ctx.getSender()))
                    .addMain(SHOOT_PACKET_ID, ShootPayload.PACKET_CODEC, (payload, ctx) -> handleShoot(ctx.getSender()))
                .clientbound()
                    .addMain(RECOIL_CLIENT_PACKET_ID, RecoilPayload.PACKET_CODEC,
                            (payload, ctx) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleRecoil(net.minecraft.client.Minecraft.getInstance(), payload))
                    .addMain(SHOOT_CLIENT_PACKET_ID, ShootClientPayload.PACKET_CODEC,
                            (payload, ctx) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleClientShoot(net.minecraft.client.Minecraft.getInstance(), payload))
                    .addMain(SHOOT_ANIMATION_PACKET_ID, ShootAnimationPayload.PACKET_CODEC,
                            (payload, ctx) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleShootAnimation(net.minecraft.client.Minecraft.getInstance()))
                    .addMain(SHOOT_PARTICLE_PACKET_ID, ShootParticlePayload.PACKET_CODEC,
                            (payload, ctx) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleShootParticle(net.minecraft.client.Minecraft.getInstance(), payload))
                    .addMain(ITEM_COMPONENT_SYNC_PACKET_ID, ItemComponentSyncPayload.PACKET_CODEC, (payload, ctx) -> {})
                    .addMain(GUN_STATE_PACKET_ID, GunStatePayload.PACKET_CODEC,
                            (payload, ctx) -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleGunState(net.minecraft.client.Minecraft.getInstance(), payload))
                .build();

        // Forces the static CHANNEL field above to actually initialize - called once from
        // TemplateModForge's constructor, same call-site role as registerAllPackets() on Fabric.
        public static void init() {}
    }
    //?} else {
    /^// 1.20.1 predates ChannelBuilder.payloadChannel()/CustomPacketPayload/StreamCodec entirely -
    // confirmed via javap against the real forge-1.20.1-47.4.22 jar (only the classic
    // NetworkRegistry.ChannelBuilder().simpleChannel() -> SimpleChannel.registerMessage(id, class,
    // encoder, decoder, handler) API exists; net.minecraftforge.network.Channel/ChannelBuilder
    // (payload-based) don't exist in this version's net.minecraftforge.network package at all).
    // Each payload's read(buf)/write(buf) methods (see the record definitions above) are reused
    // directly as the decoder/encoder function references.
    public static class ForgeNetwork {
        public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = net.minecraftforge.network.NetworkRegistry.ChannelBuilder
                .named(com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "main"))
                .networkProtocolVersion(() -> "1")
                .clientAcceptedVersions(v -> true)
                .serverAcceptedVersions(v -> true)
                .simpleChannel();

        private static int nextId = 0;

        // Forge's classic NetworkRegistry/SimpleChannel delivers every S2C packet on a local
        // (singleplayer/integrated-server) connection TWICE - confirmed empirically: every single
        // registered message type, including a trivial diagnostic packet completely unrelated to
        // guns, was independently observed via logging to reach its handler exactly twice per
        // send (once on a "Netty Local Client IO" thread, once on the Render thread one tick
        // later), with a benign "Unknown custom packet identifier" warning as an apparent
        // third/leftover delivery attempt each time. This is inside Forge's own local-connection
        // packet plumbing (see e.g. MinecraftForge PR #9157, a related but not identical
        // buffer-reuse bug in the same area for LAN sends), not something reachable from mod code,
        // so rather than chase Forge's internals further this de-duplicates by payload identity
        // (records' generated equals/hashCode compare field values) within a short window - two
        // genuinely distinct rapid shots have different recoil/particle values and are unaffected,
        // but the exact-duplicate second delivery of the same shot's effects gets dropped.
        //
        // Window must stay well under the fastest weapon's fire interval, not just "short": an
        // automatic weapon's RecoilPayload/ShootAnimationPayload carry the *same* field values
        // (or, for ShootAnimationPayload, no fields at all) on every shot, and put() below refreshes
        // the stored timestamp on every hit - so with a window that's too wide, one held-trigger
        // burst never lets the entry go stale and every shot after the first gets silently dropped
        // for the rest of the burst (confirmed in-game: auto_gun's camera-kick recoil only fired
        // once per trigger-hold; its client-predicted item-cooldown pose animation was unaffected
        // since that path never touches this network dedup at all). auto_gun's fireRate(4) is the
        // fastest currently-enabled automatic weapon (4 ticks = 200ms between shots, see
        // ItemReg.AUTO_GUN) - 100ms comfortably clears the ~1-tick (50ms) real duplicate-delivery
        // gap above while staying well under that 200ms floor.
        private static final java.util.Map<Object, Long> recentPayloads = new java.util.concurrent.ConcurrentHashMap<>();
        private static final long DEDUP_WINDOW_MS = 100;

        private static boolean isDuplicateDelivery(Object payload) {
            long now = System.currentTimeMillis();
            recentPayloads.entrySet().removeIf(e -> now - e.getValue() > DEDUP_WINDOW_MS);
            return recentPayloads.put(payload, now) != null;
        }

        public static void init() {
            // Every message MUST declare its NetworkDirection explicitly (the 6-arg
            // registerMessage overload) - confirmed against a real, working 1.20.1-forge mod's
            // networking code (MCModderAnchor/TACZ, NetworkHandler.java) after the 5-arg
            // (direction-less) overload produced client-side-only "Unknown custom packet
            // identifier" warnings for every S2C message while C2S ones worked fine. SimpleChannel
            // apparently needs the direction to correctly route/validate incoming packets on the
            // receiving side; omitting it silently breaks receipt in (at least) one direction
            // rather than erroring at registration time.
            CHANNEL.registerMessage(nextId++, ReloadPayload.class, ReloadPayload::write, ReloadPayload::read,
                    (payload, ctx) -> ctx.get().enqueueWork(() -> handleReload(ctx.get().getSender())),
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
            CHANNEL.registerMessage(nextId++, CancelReloadPayload.class, CancelReloadPayload::write, CancelReloadPayload::read,
                    (payload, ctx) -> ctx.get().enqueueWork(() -> handleCancelReload(ctx.get().getSender())),
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
            CHANNEL.registerMessage(nextId++, ShootPayload.class, ShootPayload::write, ShootPayload::read,
                    (payload, ctx) -> ctx.get().enqueueWork(() -> handleShoot(ctx.get().getSender())),
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
            CHANNEL.registerMessage(nextId++, RecoilPayload.class, RecoilPayload::write, RecoilPayload::read,
                    (payload, ctx) -> {
                        if (isDuplicateDelivery(payload)) return;
                        ctx.get().enqueueWork(() -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleRecoil(net.minecraft.client.Minecraft.getInstance(), payload));
                    },
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
            CHANNEL.registerMessage(nextId++, ShootClientPayload.class, ShootClientPayload::write, ShootClientPayload::read,
                    (payload, ctx) -> {
                        if (isDuplicateDelivery(payload)) return;
                        ctx.get().enqueueWork(() -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleClientShoot(net.minecraft.client.Minecraft.getInstance(), payload));
                    },
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
            CHANNEL.registerMessage(nextId++, ShootAnimationPayload.class, ShootAnimationPayload::write, ShootAnimationPayload::read,
                    (payload, ctx) -> {
                        if (isDuplicateDelivery(payload)) return;
                        ctx.get().enqueueWork(() -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleShootAnimation(net.minecraft.client.Minecraft.getInstance()));
                    },
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
            CHANNEL.registerMessage(nextId++, ShootParticlePayload.class, ShootParticlePayload::write, ShootParticlePayload::read,
                    (payload, ctx) -> {
                        if (isDuplicateDelivery(payload)) return;
                        ctx.get().enqueueWork(() -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleShootParticle(net.minecraft.client.Minecraft.getInstance(), payload));
                    },
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
            CHANNEL.registerMessage(nextId++, ItemComponentSyncPayload.class, ItemComponentSyncPayload::write, ItemComponentSyncPayload::read,
                    (payload, ctx) -> {},
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
            CHANNEL.registerMessage(nextId++, GunStatePayload.class, GunStatePayload::write, GunStatePayload::read,
                    (payload, ctx) -> {
                        // Idempotent (just sets state) - no dedup needed, unlike the effect payloads above.
                        ctx.get().enqueueWork(() -> com.goldbocman.vgm.common.registry.ClientNetworkReg.handleGunState(net.minecraft.client.Minecraft.getInstance(), payload));
                    },
                    java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
        }
    }
    ^///?}
    *///?}
}
