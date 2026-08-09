package com.goldbocman.vgm;

//? if forge {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.goldbocman.vgm.TemplateModCommon.*;

@Mod("vgm")
public class TemplateModForge {
    // Forge 1.21.1 (eventbus 6.2.27) predates the constructor-parameter-injection style shown in
    // newer Forge MDK examples (which pass FMLJavaModLoadingContext directly and use its
    // getModBusGroup() - an EventBus-7-only concept, see build.forge.gradle.kts's comment on the
    // eventbus-validator annotation processor). A no-arg constructor + the classic static
    // FMLJavaModLoadingContext.get() accessor works across a much wider Forge version range, so it's
    // used here instead of guessing at 1.21.1's exact constructor-injection surface.
    public TemplateModForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        com.goldbocman.vgm.common.LegacyModGuard.checkForLegacyMod();

        com.goldbocman.vgm.common.Bren.CREATIVE_MODE_TABS_FORGE.register(modEventBus);

        new com.goldbocman.vgm.common.Bren().onInitialize();

        // registerAllPackets() is a no-op stub on Forge (see NetworkReg) - this call forces
        // ForgeNetwork's static CHANNEL field to actually build, which is what really registers the
        // channel with Forge's NetworkRegistry.
        com.goldbocman.vgm.common.registry.NetworkReg.ForgeNetwork.init();

        // Forge's "data" run target still reports Dist.CLIENT (unlike Fabric/NeoForge, whose datagen
        // entrypoints are separate classes never wired through the main mod constructor at all - see
        // VgmFabricDataGenerator/VgmNeoForgeDataGen), so isClient() alone isn't enough to skip client
        // init during runData - confirmed by runData actually crashing without this extra check:
        // ClientBren.onInitializeClient() reaches registerLegacyMagazineModelPredicates(), which
        // classloads LegacyItemPropertiesAccessor (a @Mixin interface) directly, and Mixin forbids
        // that outside its own transformer ("Illegal classload request... cannot be referenced
        // directly"). DatagenModLoader.isRunningDataGen() is Forge's own way to detect this.
        if (com.goldbocman.vgm.ModLoaderAccess.INSTANCE.isClient()
                && !net.minecraftforge.data.loading.DatagenModLoader.isRunningDataGen()) {
            new com.goldbocman.vgm.client.ClientBren().onInitializeClient();
        }

        LOGGER.info("BAM! vgm (Forge) is done loading!");
    }
}
*///?}
