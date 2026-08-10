package com.goldbocman.vgm.datagen;

//? if forge {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.datagen.VgmAdvancementProvider;
import com.goldbocman.vgm.common.datagen.VgmEnchantmentBootstrap;
//? if >=1.20.5 {
import com.goldbocman.vgm.common.datagen.legacy.LegacyVgmRecipeProvider;
//?} else {
/^import com.goldbocman.vgm.common.datagen.legacy1201.Legacy1201VgmRecipeProvider;
^///?}

import java.util.List;
import java.util.concurrent.CompletableFuture;

// Forge's GatherDataEvent (net.minecraftforge.data.event) is a lower-level API than NeoForge's -
// no createProvider(...)/createDatapackRegistryObjects(...) convenience methods, just a raw
// DataGenerator (event.getGenerator().addProvider(boolean, DataProvider.Factory<T>)) and a
// CompletableFuture<HolderLookup.Provider> from event.getLookupProvider() - confirmed via the real
// forge-1.21.1-52.1.0 sources, not assumed from NeoForge parity. Only targets 1.21.1 so far (see
// claude/forge-port.md - forge phase 1 doesn't cover 26.2 yet), hence the unconditional
// LegacyVgmRecipeProvider import (no >=1.21.11 VgmRecipeProvider branch needed here yet).
@Mod.EventBusSubscriber(modid = Bren.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VgmForgeDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // Mirrors NeoForgeDataGen's <1.21.11 branch, which also gates on includeClient() only - on
        // 1.21.1 there's a single unified "data" run (see build.forge.gradle.kts's "data" run config),
        // not yet split into client/server data like >=1.21.11.
        if (!event.includeClient()) return;

        var generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        //? if >=1.20.5 {
        generator.addProvider(true, (DataProvider.Factory<LegacyVgmRecipeProvider>)
                (PackOutput output) -> new LegacyVgmRecipeProvider(output, lookupProvider));
        //?} else {
        /^// 1.20.1's RecipeProvider constructor takes only (PackOutput) - no registries param at
        // all (RecipeOutput/HolderLookup-aware datagen didn't exist yet) - see
        // Legacy1201VgmRecipeProvider's own doc comment.
        generator.addProvider(true, (DataProvider.Factory<Legacy1201VgmRecipeProvider>)
                (PackOutput output) -> new Legacy1201VgmRecipeProvider(output));
        ^///?}
        generator.addProvider(true, (DataProvider.Factory<AdvancementProvider>) (PackOutput output) ->
                new AdvancementProvider(output, lookupProvider, List.of(new VgmAdvancementProvider())));

        // No createDatapackRegistryObjects(RegistrySetBuilder) equivalent on Forge - build the
        // enchantment bootstrap's HolderLookup.Provider directly and feed it to vanilla's own
        // RegistriesDatapackGenerator (dumps every non-"minecraft"-namespace datapack registry entry),
        // rather than NeoForge/Fabric's own dynamic-registry-provider wrapper classes. Skipped
        // pre-1.20.5: VgmEnchantmentBootstrap has zero custom enchantments registered (pure future
        // scaffolding) so this provider is currently inert either way - mirrors
        // VgmFabricDataGenerator's own <1.20.5 branch (see that file's doc comment).
        //? if >=1.20.5 {
        CompletableFuture<HolderLookup.Provider> enchantmentRegistries = CompletableFuture.completedFuture(
                new RegistrySetBuilder()
                        .add(Registries.ENCHANTMENT, VgmEnchantmentBootstrap::bootstrap)
                        .build(RegistryAccess.EMPTY));
        generator.addProvider(true, (DataProvider.Factory<RegistriesDatapackGenerator>) (PackOutput output) ->
                new RegistriesDatapackGenerator(output, enchantmentRegistries));
        //?}
    }
}
*///?}
