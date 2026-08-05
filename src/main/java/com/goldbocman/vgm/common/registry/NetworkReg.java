package com.goldbocman.vgm.common.registry;

//? if fabric {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?}
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.registry.custom.MagazineItem;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkReg {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/NetworkReg");
    
    public static final CustomPacketPayload.Type<ReloadPayload> RELOAD_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "reload"));
    public static final CustomPacketPayload.Type<RecoilPayload> RECOIL_CLIENT_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "recoil_client"));
    public static final CustomPacketPayload.Type<ShootClientPayload> SHOOT_CLIENT_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "shoot_client"));
    public static final CustomPacketPayload.Type<ShootPayload> SHOOT_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "shoot"));
    public static final CustomPacketPayload.Type<ShootAnimationPayload> SHOOT_ANIMATION_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "shoot_animation"));
    public static final CustomPacketPayload.Type<ItemComponentSyncPayload> ITEM_COMPONENT_SYNC_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "item_component_sync"));
    // 添加新的射击粒子效果数据包（S2C）
    public static final CustomPacketPayload.Type<ShootParticlePayload> SHOOT_PARTICLE_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "shoot_particle"));
//    public static final CustomPacketPayload.Type<GrenadeLeftClickPayload> GRENADE_LEFT_CLICK_PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bren.MODID, "grenade_left_click"));

    private static void handleReload(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();

        if (server != null) {
            server.execute(() -> {
                ItemStack stack = player.getMainHandItem();

                if (stack.getItem() instanceof GunItem gunItem) {
                    gunItem.onReload(player);
                    // If onReload started a reload, tell all nearby clients
//                        if (player instanceof IGunUser gunUser &&
//                                gunUser.bren_1_21_1$getGunState() == GunHelper.GunStates.RELOADING) {
//                            broadcastGunState(player, true);
//                        }
                } else if (stack.getItem() instanceof MagazineItem) {
                    GunUtils.fillMagazine(stack, player);
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
    public static void registerAllPackets() {
        LOGGER.info("Registering all network packets");

        // 注册所有数据包类型
        // 客户端接收的数据包（S2C - Server to Client）
        PayloadTypeRegistry.clientboundPlay().register(RECOIL_CLIENT_PACKET_ID, RecoilPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_CLIENT_PACKET_ID, ShootClientPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_ANIMATION_PACKET_ID, ShootAnimationPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_PARTICLE_PACKET_ID, ShootParticlePayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ITEM_COMPONENT_SYNC_PACKET_ID, ItemComponentSyncPayload.PACKET_CODEC);
        // 服务器端接收的数据包（C2S - Client to Server）
        PayloadTypeRegistry.serverboundPlay().register(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SHOOT_PACKET_ID, ShootPayload.PACKET_CODEC);
//        PayloadTypeRegistry.serverboundPlay().register(GRENADE_LEFT_CLICK_PACKET_ID, GrenadeLeftClickPayload.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, (payload, context) -> handleReload(context.player()));

        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (payload, context) -> handleShoot(context.player()));

//        ServerPlayNetworking.registerGlobalReceiver(GRENADE_LEFT_CLICK_PACKET_ID, (payload, context) -> {
//            ServerPlayer player = context.player();
//            MinecraftServer server = player.level().getServer();
//
//            if (server != null) {
//                server.execute(() -> {
//                    ItemStack stack = player.getMainHandItem();
//                    com.goldbocman.vgm.common.registry.custom.types.GrenadeItem.onLeftClick(player, stack);
//                });
//            }
//        });

        LOGGER.info("All network packets registered successfully");
    }
    //?}
    //? if neoforge
    //public static void registerAllPackets() {} // NeoForge registers via NeoForgePayloads (RegisterPayloadHandlersEvent) below instead.
    
    // 重装包数据类
    public record ReloadPayload() implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ReloadPayload> PACKET_CODEC = StreamCodec.unit(new ReloadPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return RELOAD_PACKET_ID;
        }
    }
    
    // 后坐力数据包
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
    
    // 客户端射击声音数据包
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
    
    // 射击动画数据包（无数据）
    public record ShootAnimationPayload() implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, ShootAnimationPayload> PACKET_CODEC = StreamCodec.unit(new ShootAnimationPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return SHOOT_ANIMATION_PACKET_ID;
        }
    }
    
    // 射击粒子效果数据包
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
    
    // 射击粒子效果数据包（S2C）
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

//    public record GrenadeLeftClickPayload() implements CustomPacketPayload, com.goldbocman.vgm.common.registry.GrenadeLeftClickPayload {
//        public static final StreamCodec<RegistryFriendlyByteBuf, GrenadeLeftClickPayload> PACKET_CODEC = StreamCodec.unit(new GrenadeLeftClickPayload());
//
//        @Override
//        public void write(RegistryFriendlyByteBuf buf) {
//        }
//
//        @Override
//        public @NotNull Type<? extends CustomPacketPayload> type() {
//            return GRENADE_LEFT_CLICK_PACKET_ID;
//        }
//    }

    //? if neoforge {
    /*@net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID)
    public static class NeoForgePayloads {
        @net.neoforged.bus.api.SubscribeEvent
        public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
            // "1" is the payload-version string NeoForge uses to reject clients/servers with mismatched
            // channel versions - bump it if any of these payload records ever change shape.
            net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar("1");

            // 服务器端接收的数据包（C2S - Client to Server）
            registrar.playToServer(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC,
                    (payload, context) -> handleReload((net.minecraft.server.level.ServerPlayer) context.player()));
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
        }
    }
    *///?}
}