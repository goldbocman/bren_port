package com.goldbocman.vgm.common.registry;

//? if fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DataPackReg {
    //? if fabric {
    public static void init() {
        // 注册服务器启动事件，确保数据包正确加载
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        });
    }
    //?}
    //? if neoforge
    //public static void init() {} // NeoForge: no equivalent logic needed yet (Fabric's handler is a no-op too).
}
