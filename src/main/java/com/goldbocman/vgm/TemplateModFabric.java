package com.goldbocman.vgm;

//? if fabric {
import net.fabricmc.api.ModInitializer;

import static com.goldbocman.vgm.TemplateModCommon.*;

public class TemplateModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new com.goldbocman.vgm.common.Bren().onInitialize();

        //? if fapi: <0.100
        //LOGGER.info("Fabric API is old on this version");
    }
}
//?}