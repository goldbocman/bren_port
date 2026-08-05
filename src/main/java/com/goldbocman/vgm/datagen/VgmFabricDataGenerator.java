package com.goldbocman.vgm.datagen;

//? if fabric {
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import com.goldbocman.vgm.common.datagen.VgmAdvancementProvider;
import com.goldbocman.vgm.common.datagen.VgmEnchantmentBootstrap;
import com.goldbocman.vgm.common.datagen.VgmRecipeProvider;

import java.util.List;

public class VgmFabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(VgmRecipeProvider.Runner::new);
        pack.addProvider((output, registries) ->
                new AdvancementProvider(output, registries, List.of(new VgmAdvancementProvider())));
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
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, VgmEnchantmentBootstrap::bootstrap);
    }
}
//?}
