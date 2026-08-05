package com.goldbocman.vgm.common.datagen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.enchantment.Enchantment;

public final class VgmEnchantmentBootstrap {
    private VgmEnchantmentBootstrap() {
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        // No custom enchantments exist yet. Register future ones here, e.g.:
        //
        // context.register(
        //     ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(Bren.MODID, "example")),
        //     new Enchantment(
        //         Component.translatable("enchantment.vgm.example"),
        //         Enchantment.definition(
        //             context.lookup(Registries.ITEM).getOrThrow(TagReg.WEAPONS),
        //             10, 3,
        //             Enchantment.constantCost(1), Enchantment.constantCost(11),
        //             1, EquipmentSlotGroup.MAINHAND),
        //         HolderSet.empty(), DataComponentMap.EMPTY));
    }
}
