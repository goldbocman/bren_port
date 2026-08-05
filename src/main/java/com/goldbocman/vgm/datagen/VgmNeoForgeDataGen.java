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
import com.goldbocman.vgm.common.datagen.VgmRecipeProvider;

import java.util.List;

@EventBusSubscriber(modid = Bren.MODID)
public class VgmNeoForgeDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(VgmRecipeProvider.Runner::new);
        event.createProvider((output, registries) ->
                new AdvancementProvider(output, registries, List.of(new VgmAdvancementProvider())));
        event.createDatapackRegistryObjects(
                new RegistrySetBuilder().add(Registries.ENCHANTMENT, VgmEnchantmentBootstrap::bootstrap));
    }
}
*///?}
