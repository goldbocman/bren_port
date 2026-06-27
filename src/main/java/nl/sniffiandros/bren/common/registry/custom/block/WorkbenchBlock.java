package nl.sniffiandros.bren.common.registry.custom.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// TODO: BALANCING NEEDED — workbench block is not registered anywhere (no BlockReg). Assets and recipes
//  exist in resources/. To re-enable: create a block registration and add a BlockItem to ItemReg.
public class WorkbenchBlock extends Block {

    public VoxelShape shape(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0, 0, 0, 1, 0.875, 1));

        return shape;
    }

    public WorkbenchBlock(Properties settings) {
        super(settings.noOcclusion());
    }


}
