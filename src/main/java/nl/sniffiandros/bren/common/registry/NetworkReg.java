package nl.sniffiandros.bren.common.registry;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;
import nl.sniffiandros.bren.common.utils.GunUtils;
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

    public static void registerAllPackets() {
        LOGGER.info("Registering all network packets");
        
        // 注册所有数据包类型
        // 客户端接收的数据包（S2C - Server to Client）
        PayloadTypeRegistry.clientboundPlay().register(RECOIL_CLIENT_PACKET_ID, RecoilPayload.PACKET_CODEC); // Changed from PayloadTypeRegistry.playS2C() to PayloadTypeRegistry.playS2C()
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_CLIENT_PACKET_ID, ShootClientPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_ANIMATION_PACKET_ID, ShootAnimationPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SHOOT_PARTICLE_PACKET_ID, ShootParticlePayload.PACKET_CODEC); // 新的S2C数据包
        PayloadTypeRegistry.clientboundPlay().register(ITEM_COMPONENT_SYNC_PACKET_ID, ItemComponentSyncPayload.PACKET_CODEC); // 物品组件同步包
        // 服务器端接收的数据包（C2S - Client to Server）
        PayloadTypeRegistry.serverboundPlay().register(RELOAD_PACKET_ID, ReloadPayload.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SHOOT_PACKET_ID, ShootPayload.PACKET_CODEC);
//        PayloadTypeRegistry.serverboundPlay().register(GRENADE_LEFT_CLICK_PACKET_ID, GrenadeLeftClickPayload.PACKET_CODEC);
    
        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = player.level().getServer();
            
            LOGGER.info("Received RELOAD packet from player: {}, server: {}", 
                player.getName().getString(), server != null ? "available" : "null");
            
            if (server != null) {
                server.execute(() -> {
                    ItemStack stack = player.getMainHandItem();

                    LOGGER.info("Processing reload for player: {}, main hand item: {}", 
                        player.getName().getString(), stack.getItem().toString());

                    if (stack.getItem() instanceof GunItem gunItem) {
                        LOGGER.info("Calling onReload for GunItem: {}", stack.getItem().toString());
                        // 修复：移除重复设置reloadingGun的逻辑
                        // onReload方法内部已经设置了reloadingGun
                        gunItem.onReload(player);
                    } else if (stack.getItem() instanceof MagazineItem) {
                        LOGGER.info("Calling fillMagazine for MagazineItem");
                        GunUtils.fillMagazine(stack, player);
                    } else {
                        LOGGER.warn("Player {} attempted to reload with non-gun/magazine item: {}", 
                            player.getName().getString(), stack.getItem().toString());
                    }
                });
            } else {
                LOGGER.error("Server is null for player {}", player.getName().getString());
            }
        });
        
        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (payload, context) -> {
            // 获取玩家和主手物品
            // 检查是否为GunItem
            // 调用GunUtils.fire方法执行射击逻辑
            // 设置冷却时间
            // 发送粒子效果到客户端
            // 触发射击事件
        });
        
//        ServerPlayNetworking.registerGlobalReceiver(GRENADE_LEFT_CLICK_PACKET_ID, (payload, context) -> {
//            ServerPlayer player = context.player();
//            MinecraftServer server = player.level().getServer();
//
//            if (server != null) {
//                server.execute(() -> {
//                    ItemStack stack = player.getMainHandItem();
//                    nl.sniffiandros.bren.common.registry.custom.types.GrenadeItem.onLeftClick(player, stack);
//                });
//            }
//        });
        
        LOGGER.info("All network packets registered successfully");
    }
    
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

//    public record GrenadeLeftClickPayload() implements CustomPacketPayload, nl.sniffiandros.bren.common.registry.GrenadeLeftClickPayload {
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


}