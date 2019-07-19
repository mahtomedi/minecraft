package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;

public class SliceShape extends VoxelShape {
    private final VoxelShape delegate;
    private final Direction.Axis axis;
    private static final DoubleList SLICE_COORDS = new CubePointRange(1);

    public SliceShape(VoxelShape param0, Direction.Axis param1, int param2) {
        super(makeSlice(param0.shape, param1, param2));
        this.delegate = param0;
        this.axis = param1;
    }

    private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape param0, Direction.Axis param1, int param2) {
        return new SubShape(
            param0,
            param1.choose(param2, 0, 0),
            param1.choose(0, param2, 0),
            param1.choose(0, 0, param2),
            param1.choose(param2 + 1, param0.xSize, param0.xSize),
            param1.choose(param0.ySize, param2 + 1, param0.ySize),
            param1.choose(param0.zSize, param0.zSize, param2 + 1)
        );
    }

    @Override
    protected DoubleList getCoords(Direction.Axis param0) {
        return param0 == this.axis ? SLICE_COORDS : this.delegate.getCoords(param0);
    }
}
