package com.goldbocman.vgm.common.registry;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DataPackReg {
    public static void init() {
        // 注册服务器启动事件，确保数据包正确加载
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        });
    }
}