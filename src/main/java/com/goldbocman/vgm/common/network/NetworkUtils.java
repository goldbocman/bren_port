package com.goldbocman.vgm.common.network;

//? if fabric
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? if >=1.20.5 {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
*///?} else {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.goldbocman.vgm.common.registry.NetworkReg;

public class NetworkUtils {

    // The parameter type itself has to differ by version (CustomPacketPayload doesn't exist before
    // 1.20.5), so the split happens at the method level via two small per-version helper methods
    // rather than trying to nest a version conditional inside the existing per-loader (fabric/
    // neoforge/forge) one below - keeps each existing loader-conditional block at the same nesting
    // depth it already had, so none of it needs re-escaping for an extra nesting level.
    //? if >=1.20.5 {
    /*public static void sendToPlayer(Player player, CustomPacketPayload payload) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        //? if fabric {
        /^ServerPlayNetworking.send(serverPlayer, payload);
        ^///?}
        //? if neoforge
        //net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, payload);
        //? if forge
        NetworkReg.ForgeNetwork.CHANNEL.send(payload, net.minecraftforge.network.PacketDistributor.PLAYER.with(serverPlayer));
    }
    *///?} else {
    // 1.20.1 predates CustomPacketPayload - accepts any of NetworkReg's legacy payload records.
    // NeoForge never targets <1.20.5, so only fabric/forge need a branch here.
    public static void sendToPlayer(Player player, Object payload) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        //? if fabric {
        /*// Dispatches on the concrete payload type to find its channel id and
        // write(FriendlyByteBuf) method (every payload record has one, see NetworkReg).
        FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        writeLegacyPayload(buf, payload);
        ServerPlayNetworking.send(serverPlayer, legacyChannelId(payload), buf);
        *///?} elif forge {
        // SimpleChannel.send/sendTo take the payload object directly (dispatched by registered
        // class - see NetworkReg.ForgeNetwork.init()), no manual buffer needed. Unlike the
        // >=1.20.5 Channel API, this version's PacketDistributor.with() takes a Supplier<T>, not
        // T directly (confirmed via javap against forge-1.20.1-47.4.22). Forge's classic
        // NetworkRegistry/SimpleChannel delivers every S2C packet twice on a local connection -
        // see NetworkReg.ForgeNetwork's dedup note for the confirmed details - handled there via
        // per-payload deduplication rather than here.
        NetworkReg.ForgeNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer), payload);
        //?}
    }
    //?}

    // Client -> server send, used from common-loaded code (e.g. PlayerEntityMixin) that must never
    // reference a client-only networking class directly - the loader-specific class reference lives
    // only here, behind this loader-agnostic helper.
    //? if >=1.20.5 {
    /*public static void sendToServer(CustomPacketPayload payload) {
        //? if fabric {
        /^net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
        ^///?}
        // 1.21.1's PacketDistributor has sendToServer(...) directly - no separate client-only class yet.
        //? if neoforge {
        /^//? if >=1.21.11 {
        /^¹net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
        ¹^///?} else {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(payload);
        //?}
        ^///?}
        //? if forge
        NetworkReg.ForgeNetwork.CHANNEL.send(payload, net.minecraftforge.network.PacketDistributor.SERVER.noArg());
    }
    *///?} else {
    // NeoForge never targets <1.20.5, so only fabric/forge need a branch here.
    public static void sendToServer(Object payload) {
        //? if fabric {
        /*FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        writeLegacyPayload(buf, payload);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(legacyChannelId(payload), buf);
        *///?} elif forge {
        NetworkReg.ForgeNetwork.CHANNEL.sendToServer(payload);
        //?}
    }
    //?}

    //? if <1.20.5 {
    private static ResourceLocation legacyChannelId(Object payload) {
        if (payload instanceof NetworkReg.ReloadPayload) return NetworkReg.RELOAD_PACKET_ID;
        if (payload instanceof NetworkReg.CancelReloadPayload) return NetworkReg.CANCEL_RELOAD_PACKET_ID;
        if (payload instanceof NetworkReg.RecoilPayload) return NetworkReg.RECOIL_CLIENT_PACKET_ID;
        if (payload instanceof NetworkReg.ShootClientPayload) return NetworkReg.SHOOT_CLIENT_PACKET_ID;
        if (payload instanceof NetworkReg.ShootPayload) return NetworkReg.SHOOT_PACKET_ID;
        if (payload instanceof NetworkReg.ShootAnimationPayload) return NetworkReg.SHOOT_ANIMATION_PACKET_ID;
        if (payload instanceof NetworkReg.ItemComponentSyncPayload) return NetworkReg.ITEM_COMPONENT_SYNC_PACKET_ID;
        if (payload instanceof NetworkReg.GunStatePayload) return NetworkReg.GUN_STATE_PACKET_ID;
        if (payload instanceof NetworkReg.ShootParticlePayload) return NetworkReg.SHOOT_PARTICLE_PACKET_ID;
        throw new IllegalArgumentException("Unknown legacy payload type: " + payload.getClass());
    }

    private static void writeLegacyPayload(FriendlyByteBuf buf, Object payload) {
        if (payload instanceof NetworkReg.ReloadPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.CancelReloadPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.RecoilPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.ShootClientPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.ShootPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.ShootAnimationPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.ItemComponentSyncPayload p) p.write(buf);
        else if (payload instanceof NetworkReg.GunStatePayload p) p.write(buf);
        else if (payload instanceof NetworkReg.ShootParticlePayload p) p.write(buf);
        else throw new IllegalArgumentException("Unknown legacy payload type: " + payload.getClass());
    }
    //?}

    public static void sendShotEffect(Player player, Vec3 origin, Vec3 direction, boolean ejectCasing) {
        NetworkReg.ShootParticlePayload payload = new NetworkReg.ShootParticlePayload(
            (float) origin.x, (float) origin.y, (float) origin.z,
            (float) direction.x, (float) direction.y, (float) direction.z,
            ejectCasing
        );
        sendToPlayer(player, payload);
    }
}
