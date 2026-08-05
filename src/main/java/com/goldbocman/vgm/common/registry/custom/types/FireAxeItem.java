package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ToolMaterial;

public class FireAxeItem extends AxeItem {

    public FireAxeItem(ToolMaterial toolMaterial, float f, float g, Properties properties) {
        super(toolMaterial, f, g, properties);
    }

    public FireAxeItem(Properties properties) {
        super(VanillaToolMaterials.NETHERITE, 6.0F, -3.0F, properties);
    }

}
