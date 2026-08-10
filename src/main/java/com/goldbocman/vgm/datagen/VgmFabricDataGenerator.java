package com.goldbocman.vgm.datagen;

//? if fabric {
/*import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import com.goldbocman.vgm.common.datagen.VgmAdvancementProvider;
import com.goldbocman.vgm.common.datagen.VgmEnchantmentBootstrap;
//? if >=1.21.11 {
/^import com.goldbocman.vgm.common.datagen.VgmRecipeProvider;
^///?} else {
//? if >=1.20.5 {
import com.goldbocman.vgm.common.datagen.legacy.LegacyVgmRecipeProvider;
//?} else {
/^import com.goldbocman.vgm.common.datagen.legacy1201.Legacy1201VgmRecipeProvider;
^///?}
//?}

import java.util.List;

public class VgmFabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        //? if >=1.21.11 {
        /^pack.addProvider(VgmRecipeProvider.Runner::new);
        ^///?} else {
        //? if >=1.20.5 {
        pack.addProvider(LegacyVgmRecipeProvider::new);
        //?} else {
        /^pack.addProvider((net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack.Factory<Legacy1201VgmRecipeProvider>)
                Legacy1201VgmRecipeProvider::new);
        ^///?}
        //?}
        pack.addProvider((output, registries) ->
                new AdvancementProvider(output, registries, List.of(new VgmAdvancementProvider())));
        //? if >=1.20.5 {
        pack.addProvider((output, registries) -> new FabricDynamicRegistryProvider(output, registries) {
            @Override
            protected void configure(HolderLookup.Provider reg, Entries entries) {
                entries.addAll(reg.lookupOrThrow(Registries.ENCHANTMENT));
            }

            @Override
            public String getName() {
                return "VGM Enchantments";
            }
        });
        //?}
    }

    // VgmEnchantmentBootstrap has zero custom enchantments registered (pure future scaffolding), so
    // this override - and the FabricDynamicRegistryProvider above - are currently inert either way.
    // At 1.20.1, fabric-data-generation-api-v1's own vanilla RegistrySetBuilder merge already
    // supplies an ENCHANTMENT registry entry, and adding a second one here collides
    // ("Multiple entries with same key: minecraft:enchantment") and crashes runDatagen - confirmed
    // empirically, not assumed. Since there's nothing to register yet, just skip the override.
    //? if >=1.20.5 {
    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, VgmEnchantmentBootstrap::bootstrap);
    }
    //?}
}
*///?}
