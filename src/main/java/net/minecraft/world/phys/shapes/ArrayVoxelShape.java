package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.Util;
import net.minecraft.core.Direction;

public class ArrayVoxelShape extends VoxelShape {
    private final DoubleList xs;
    private final DoubleList ys;
    private final DoubleList zs;

    ArrayVoxelShape(DiscreteVoxelShape param0, DoubleList param1, DoubleList param2, DoubleList param3) {
        super(param0);
        int var0 = param0.getXSize() + 1;
        int var1 = param0.getYSize() + 1;
        int var2 = param0.getZSize() + 1;
        if (var0 == param1.size() && var1 == param2.size() && var2 == param3.size()) {
            this.xs = param1;
            this.ys = param2;
            this.zs = param3;
        } else {
            throw (IllegalArgumentException)Util.pauseInIde(
                new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape.")
            );
        }
    }

    @Override
    protected DoubleList getCoords(Direction.Axis param0) {
        switch(param0) {
            case X:
                return this.xs;
            case Y:
                return this.ys;
            case Z:
                return this.zs;
            default:
                throw new IllegalArgumentException();
        }
    }
}
