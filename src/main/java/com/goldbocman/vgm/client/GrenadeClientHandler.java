package com.goldbocman.vgm.client;

//? if fabric
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrenadeClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrenadeClientHandler.class);
    private static boolean wasLeftClickDown = false;

    static void onEndClientTick(net.minecraft.client.Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        Player player = client.player;
        boolean isLeftClickDown = client.options.keyAttack.isDown();

        wasLeftClickDown = isLeftClickDown;
    }

    //? if fabric {
    /*public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(GrenadeClientHandler::onEndClientTick);

        LOGGER.info("GrenadeClientHandler registered successfully");
    }
    *///?}
    //? if neoforge
    //public static void register() {} // NeoForge registers via NeoForgeGrenadeTick below instead.

    //? if neoforge {
    /*@net.neoforged.fml.common.EventBusSubscriber(modid = com.goldbocman.vgm.common.Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class NeoForgeGrenadeTick {
        @net.neoforged.bus.api.SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            onEndClientTick(net.minecraft.client.Minecraft.getInstance());
        }
    }
    *///?}

    //? if forge
    public static void register() {} // Forge registers via ForgeGrenadeTick below instead.

    //? if forge {
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = com.goldbocman.vgm.common.Bren.MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ForgeGrenadeTick {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        //? if >=1.20.5 {
        /*public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent.Post event) {
            onEndClientTick(net.minecraft.client.Minecraft.getInstance());
        }
        *///?} else {
        // 1.20.1's Forge has no ClientTickEvent.Pre/Post split yet - just one class with a
        // Phase field (confirmed via javap against forge-1.20.1-47.4.22) - END mirrors .Post.
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                onEndClientTick(net.minecraft.client.Minecraft.getInstance());
            }
        }
        //?}
    }
    //?}
}
