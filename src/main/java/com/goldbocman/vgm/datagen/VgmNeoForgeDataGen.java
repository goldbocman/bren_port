package com.goldbocman.vgm.datagen;

//? if neoforge {
/*import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.datagen.VgmAdvancementProvider;
import com.goldbocman.vgm.common.datagen.VgmEnchantmentBootstrap;
//? if >=1.21.11 {
/^import com.goldbocman.vgm.common.datagen.VgmRecipeProvider;
^///?} else {
import com.goldbocman.vgm.common.datagen.legacy.LegacyVgmRecipeProvider;
//?}

import java.util.List;

//? if >=1.21.9 {
/^@EventBusSubscriber(modid = Bren.MODID)
^///?} else {
@EventBusSubscriber(modid = Bren.MODID, bus = EventBusSubscriber.Bus.MOD)
//?}
public class VgmNeoForgeDataGen {
    //? if >=1.21.11 {
    /^@SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(VgmRecipeProvider.Runner::new);
        event.createProvider((output, registries) ->
                new AdvancementProvider(output, registries, List.of(new VgmAdvancementProvider())));
        event.createDatapackRegistryObjects(
                new RegistrySetBuilder().add(Registries.ENCHANTMENT, VgmEnchantmentBootstrap::bootstrap));
    }
    ^///?} else {
    // GatherDataEvent has no nested .Client class pre-1.21.11 - it's a single flat event with an
    // includeClient() guard instead, confirmed against the real 1.21.1 NeoForge jar (not assumed).
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (!event.includeClient()) return;
        event.createProvider(LegacyVgmRecipeProvider::new);
        event.createProvider((output, registries) ->
                new AdvancementProvider(output, registries, List.of(new VgmAdvancementProvider())));
        event.createDatapackRegistryObjects(
                new RegistrySetBuilder().add(Registries.ENCHANTMENT, VgmEnchantmentBootstrap::bootstrap));
    }
    //?}
}
*///?}
