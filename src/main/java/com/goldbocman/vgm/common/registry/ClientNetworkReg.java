package com.goldbocman.vgm.common.registry;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.goldbocman.vgm.client.renderer.RecoilSys;
import com.goldbocman.vgm.client.renderer.WeaponTickHolder;
import com.goldbocman.vgm.common.config.MConfig;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;

import static com.goldbocman.vgm.common.registry.NetworkReg.SHOOT_PACKET_ID;

public class ClientNetworkReg {
    public static void shootAnimationPacket() {
        ClientPlayNetworking.registerReceiver(NetworkReg.SHOOT_ANIMATION_PACKET_ID, (payload, context) -> WeaponTickHolder.setTicks(16));
    }

    public static void recoilPacket() {
        ClientPlayNetworking.registerReceiver(NetworkReg.RECOIL_CLIENT_PACKET_ID, (payload, context) -> {
            if (context.client().player == null) {return;}
            RecoilSys.shotEvent(context.client().player, payload.recoil());
        });
    }

    public static void clientShootPacket() {
        ClientPlayNetworking.registerReceiver(NetworkReg.SHOOT_CLIENT_PACKET_ID, (payload, context) -> {
            Level world = context.client().level;
            if (world != null) {
                SoundInstance soundInstance = SimpleSoundInstance.forUI(SoundReg.ITEM_DISTANT_GUNFIRE, 1.0F - (world.getRandom().nextFloat() - 0.5F)/8, payload.volume());
                context.client().getSoundManager().play(soundInstance);
            }
        });
    }

    public static void shootParticlePacket() {
        ClientPlayNetworking.registerReceiver(NetworkReg.SHOOT_PARTICLE_PACKET_ID, (payload, context) -> {
            Level world = context.client().level;
            if (world == null) return;

            Vec3 origin = new Vec3(payload.originX(), payload.originY(), payload.originZ());
            Vec3 direction = new Vec3(payload.directionX(), payload.directionY(), payload.directionZ());

            // 调用现有的射击粒子效果
            GunItem.shotParticles(world, origin, direction, world.getRandom());

            // 如果需要弹出弹壳，调用弹壳粒子效果
            if (payload.casing() && MConfig.spawnCasingParticles.get()) {
                GunItem.ejectCasingParticle(world, origin, direction, world.getRandom());
            }
        });
    }

    public static void shootPacket() {
        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (payload, context) -> {
            // 射击包处理逻辑可以在这里添加
        });
    }
}