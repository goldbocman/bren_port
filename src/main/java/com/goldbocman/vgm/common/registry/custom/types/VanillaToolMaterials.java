package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;

public class VanillaToolMaterials {
    public static final ToolMaterial IRON = new ToolMaterial(
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "incorrect_for_iron_tool")), 
        250, 6.0F, 2.0F, 14, 
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "iron_ingot")));
        
    public static final ToolMaterial NETHERITE = new ToolMaterial(
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "incorrect_for_netherite_tool")), 
        2031, 9.0F, 6.0F, 15, 
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "netherite_ingot")));
}