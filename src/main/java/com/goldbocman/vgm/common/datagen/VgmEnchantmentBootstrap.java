package com.goldbocman.vgm.common.datagen;

//? if >=1.20.5 {
/*import net.minecraft.data.worldgen.BootstrapContext;
*///?} else {
// 1.20.1's class is spelled "BootstapContext" (missing the second "r") - confirmed via javap on
// the real 1.20.1 merged jar, a genuine Mojang mapping typo, not a repo mistake.
import net.minecraft.data.worldgen.BootstapContext;
//?}
import net.minecraft.world.item.enchantment.Enchantment;

public final class VgmEnchantmentBootstrap {
    private VgmEnchantmentBootstrap() {
    }

    //? if >=1.20.5 {
    /*public static void bootstrap(BootstrapContext<Enchantment> context) {
    *///?} else {
    public static void bootstrap(BootstapContext<Enchantment> context) {
    //?}
        // No custom enchantments exist yet. Register future ones here, e.g.:
        //
        // context.register(
        //     ResourceKey.create(Registries.ENCHANTMENT, com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "example")),
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
