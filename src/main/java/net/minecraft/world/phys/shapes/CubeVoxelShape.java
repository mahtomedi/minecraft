package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class CubeVoxelShape extends VoxelShape {
    protected CubeVoxelShape(DiscreteVoxelShape param0) {
        super(param0);
    }

    @Override
    protected DoubleList getCoords(Direction.Axis param0) {
        return new CubePointRange(this.shape.getSize(param0));
    }

    @Override
    protected int findIndex(Direction.Axis param0, double param1) {
        int var0 = this.shape.getSize(param0);
        return Mth.floor(Mth.clamp(param1 * (double)var0, -1.0, (double)var0));
    }
}
