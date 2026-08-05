package com.goldbocman.vgm.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.goldbocman.vgm.common.registry.NetworkReg;

public class NetworkUtils {

    public static void sendShotEffect(Player player, Vec3 origin, Vec3 direction, boolean ejectCasing) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkReg.ShootParticlePayload payload = new NetworkReg.ShootParticlePayload(
                (float) origin.x, (float) origin.y, (float) origin.z,
                (float) direction.x, (float) direction.y, (float) direction.z,
                ejectCasing
            );
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }

    public static void sendDataToClient(Player player, Identifier packetId, FriendlyByteBuf buf) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 根据packet ID创建对应的payload
        CustomPacketPayload payload = null;
        
        if (packetId.equals(NetworkReg.RECOIL_CLIENT_PACKET_ID.id())) {
            payload = new NetworkReg.RecoilPayload(buf.readFloat());
        } else if (packetId.equals(NetworkReg.SHOOT_CLIENT_PACKET_ID.id())) {
            payload = new NetworkReg.ShootClientPayload(buf.readFloat());
        } else if (packetId.equals(NetworkReg.SHOOT_ANIMATION_PACKET_ID.id())) {
            payload = new NetworkReg.ShootAnimationPayload();
        }
        
        if (payload != null) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }
}