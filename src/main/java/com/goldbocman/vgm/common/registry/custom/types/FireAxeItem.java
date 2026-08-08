package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.world.item.AxeItem;

public class FireAxeItem extends AxeItem {

    //? if >=1.21.11 {
    public FireAxeItem(net.minecraft.world.item.ToolMaterial toolMaterial, float f, float g, Properties properties) {
        super(toolMaterial, f, g, properties);
    }
    //?} else {
    /*// 1.21.1's AxeItem(Tier, Properties) has no separate attack-damage/speed override - f/g are kept
    // in the signature for API parity but unused, since this item is currently unregistered anyway.
    public FireAxeItem(net.minecraft.world.item.Tier tier, float f, float g, Properties properties) {
        super(tier, properties);
    }
    *///?}

    public FireAxeItem(Properties properties) {
        this(VanillaToolMaterials.NETHERITE, 6.0F, -3.0F, properties);
    }

}
