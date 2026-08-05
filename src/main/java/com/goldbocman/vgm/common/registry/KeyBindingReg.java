package com.goldbocman.vgm.common.registry;

import com.mojang.blaze3d.platform.InputConstants;
//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?}
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.network.NetworkUtils;

public class KeyBindingReg {
    public static final String KEY_RELOAD = "key.vgm.reload";

    public static KeyMapping reloadKey = new KeyMapping(
            KEY_RELOAD,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyMapping.Category.GAMEPLAY
    );

    static void consumeReloadClicks() {
        while (reloadKey.consumeClick()) {
            NetworkUtils.sendToServer(new NetworkReg.ReloadPayload());
        }
    }

    //? if fabric {
    public static void registerKeyInputs() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> consumeReloadClicks());
    }

    public static void reg() {
        reloadKey = KeyMappingHelper.registerKeyMapping(reloadKey);
        registerKeyInputs();
    }
    //?}
    //? if neoforge
    //public static void reg() {} // NeoForge registers via NeoForgeKeyBindings/NeoForgeKeyTick below instead.

    //? if neoforge {
    /*@net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class NeoForgeKeyBindings {
        @net.neoforged.bus.api.SubscribeEvent
        public static void register(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
            event.register(reloadKey);
        }
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class NeoForgeKeyTick {
        @net.neoforged.bus.api.SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
            consumeReloadClicks();
        }
    }
    *///?}
}
