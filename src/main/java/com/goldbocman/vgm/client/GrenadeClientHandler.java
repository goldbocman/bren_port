package com.goldbocman.vgm.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrenadeClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrenadeClientHandler.class);
    private static boolean wasLeftClickDown = false;
    
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }
            
            Player player = client.player;
            boolean isLeftClickDown = client.options.keyAttack.isDown();
            
            wasLeftClickDown = isLeftClickDown;
        });
        
        LOGGER.info("GrenadeClientHandler registered successfully");
    }
}