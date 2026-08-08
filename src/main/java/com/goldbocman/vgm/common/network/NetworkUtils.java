package com.goldbocman.vgm.common.network;

//? if fabric
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.goldbocman.vgm.common.registry.NetworkReg;

public class NetworkUtils {

    public static void sendToPlayer(Player player, CustomPacketPayload payload) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        //? if fabric {
        ServerPlayNetworking.send(serverPlayer, payload);
        //?}
        //? if neoforge
        //net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, payload);
    }

    // Client -> server send, used from common-loaded code (e.g. PlayerEntityMixin) that must never
    // reference a client-only networking class directly - the loader-specific class reference lives
    // only here, behind this loader-agnostic helper.
    public static void sendToServer(CustomPacketPayload payload) {
        //? if fabric {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
        //?}
        // 1.21.1's PacketDistributor has sendToServer(...) directly - no separate client-only class yet.
        //? if neoforge {
        /*//? if >=1.21.11 {
        /^net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
        ^///?} else {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(payload);
        //?}
        *///?}
    }

    public static void sendShotEffect(Player player, Vec3 origin, Vec3 direction, boolean ejectCasing) {
        NetworkReg.ShootParticlePayload payload = new NetworkReg.ShootParticlePayload(
            (float) origin.x, (float) origin.y, (float) origin.z,
            (float) direction.x, (float) direction.y, (float) direction.z,
            ejectCasing
        );
        sendToPlayer(player, payload);
    }
}
