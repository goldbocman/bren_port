package com.goldbocman.vgm.common.registry;

//? if >=1.20.5 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
*///?} else {
import net.minecraft.network.FriendlyByteBuf;
//?}

public interface GrenadeLeftClickPayload {
    //? if >=1.20.5 {
    /*void write(RegistryFriendlyByteBuf buf);
    *///?} else {
    void write(FriendlyByteBuf buf);
    //?}
}
