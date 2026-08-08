package com.goldbocman.vgm.common.registry.custom.types;

//? if >=1.21.11 {
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
//?}

// FireAxeItem/ExplosiveSpearItem (the only consumers) are both currently unregistered/commented out
// in ItemReg.java - this class only needs to compile, not be exercised at runtime. 1.21.1 predates
// the ToolMaterial record entirely (tool stats were a Tier interface + Tiers enum instead); since
// IRON/NETHERITE above are just vanilla's own real Iron/Netherite tier numbers re-declared, the
// legacy branch reuses the builtin Tiers.IRON/Tiers.NETHERITE constants directly instead of
// reconstructing them.
public class VanillaToolMaterials {
    //? if >=1.21.11 {
    public static final ToolMaterial IRON = new ToolMaterial(
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "incorrect_for_iron_tool")),
        250, 6.0F, 2.0F, 14,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "iron_ingot")));

    public static final ToolMaterial NETHERITE = new ToolMaterial(
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "incorrect_for_netherite_tool")),
        2031, 9.0F, 6.0F, 15,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "netherite_ingot")));
    //?} else {
    /*public static final net.minecraft.world.item.Tier IRON = net.minecraft.world.item.Tiers.IRON;
    public static final net.minecraft.world.item.Tier NETHERITE = net.minecraft.world.item.Tiers.NETHERITE;
    *///?}
}