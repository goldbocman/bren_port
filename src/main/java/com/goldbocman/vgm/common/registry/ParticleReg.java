package com.goldbocman.vgm.common.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import com.goldbocman.vgm.common.Bren;

public class ParticleReg {
    // 在Minecraft 1.21.1中，使用SimpleParticleType而不是泛型参数
    public static final ParticleType MUZZLE_SMOKE_PARTICLE = FabricParticleTypes.simple();
    public static final ParticleType AIR_RING_PARTICLE = FabricParticleTypes.simple();
    public static final ParticleType CASING_PARTICLE = FabricParticleTypes.simple();

    public static void reg() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Bren.MODID, "muzzle_smoke"),
                MUZZLE_SMOKE_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Bren.MODID, "air_ring"),
                AIR_RING_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Bren.MODID, "casing"),
                CASING_PARTICLE);
    }
}