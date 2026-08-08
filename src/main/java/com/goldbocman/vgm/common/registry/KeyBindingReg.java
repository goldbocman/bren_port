package com.goldbocman.vgm.common.registry;

import com.mojang.blaze3d.platform.InputConstants;
//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}
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
            //? if >=1.21.11 {
            KeyMapping.Category.GAMEPLAY
            //?} else {
            /*// Pre-1.21.11 KeyMapping takes the category as a plain translation-key string instead of
            // the newer Category enum - "key.categories.gameplay" is vanilla's own gameplay category.
            "key.categories.gameplay"
            *///?}
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
        //? if >=26.1 {
        reloadKey = KeyMappingHelper.registerKeyMapping(reloadKey);
        //?} else {
        /*reloadKey = KeyBindingHelper.registerKeyBinding(reloadKey);
        *///?}
        registerKeyInputs();
    }
    //?}
    //? if neoforge
    //public static void reg() {} // NeoForge registers via NeoForgeKeyBindings/NeoForgeKeyTick below instead.

    //? if neoforge {
    /*//? if >=1.21.9 {
    /^@net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    ^///?} else {
    @net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
    //?}
    public static class NeoForgeKeyBindings {
        @net.neoforged.bus.api.SubscribeEvent
        public static void register(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
            event.register(reloadKey);
        }
    }

    // Game-bus subscriber (ClientTickEvent) - Bus.GAME is the default on every version, no gating needed.
    @net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class NeoForgeKeyTick {
        @net.neoforged.bus.api.SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
            consumeReloadClicks();
        }
    }
    *///?}
}
