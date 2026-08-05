package com.goldbocman.vgm.common.registry;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface GrenadeLeftClickPayload {
    void write(RegistryFriendlyByteBuf buf);
}
