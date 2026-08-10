package com.goldbocman.vgm;

//? if forge {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

        // Registering ForgeNetwork's SimpleChannel/messages directly from the mod constructor
        // (too early - before FMLCommonSetupEvent) let the channel negotiate fine at login but
        // silently broke S2C packet delivery at 1.20.1 (client kept logging "Unknown custom
        // packet identifier: vgm:main" for every S2C message while C2S ones worked) - confirmed
        // by cross-checking two real, working 1.20.1-forge gun mods' networking setup
        // (MCModderAnchor/TACZ's NetworkHandler.init(), Jetug/CreateGunMod's PacketHandler.init())
        // and finding both defer registration to FMLCommonSetupEvent via event.enqueueWork(...),
        // never the constructor. Matches the same pattern here for both Forge targets.
        modEventBus.addListener(TemplateModForge::commonSetup);

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

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(com.goldbocman.vgm.common.registry.NetworkReg.ForgeNetwork::init);
    }
}
*///?}
