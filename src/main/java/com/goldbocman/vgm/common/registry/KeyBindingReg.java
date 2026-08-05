package com.goldbocman.vgm.common.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindingReg {
    public static final String KEY_RELOAD = "key.vgm.reload";

    public static KeyMapping reloadKey;

    public static void registerKeyInputs() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            while (reloadKey.consumeClick()) {
                ClientPlayNetworking.send(new NetworkReg.ReloadPayload());
            }
        });
    }

    public static void reg() {
        reloadKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                KEY_RELOAD,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.GAMEPLAY
        ));

        registerKeyInputs();
    }
}