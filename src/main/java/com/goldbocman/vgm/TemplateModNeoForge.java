package com.goldbocman.vgm;

//? if neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import static com.goldbocman.vgm.TemplateModCommon.*;

@Mod("vgm")
public class TemplateModNeoForge {
    public TemplateModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        com.goldbocman.vgm.common.LegacyModGuard.checkForLegacyMod();

        com.goldbocman.vgm.common.Bren.CREATIVE_MODE_TABS.register(modEventBus);

        new com.goldbocman.vgm.common.Bren().onInitialize();

        if (com.goldbocman.vgm.ModLoaderAccess.INSTANCE.isClient()) {
            new com.goldbocman.vgm.client.ClientBren().onInitializeClient();
        }

        LOGGER.info("BAM! vgm (NeoForge) is done loading!");
    }
}
*///?}