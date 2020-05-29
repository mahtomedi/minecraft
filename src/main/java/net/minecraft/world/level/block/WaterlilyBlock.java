package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterlilyBlock extends BushBlock {
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.5, 15.0);

    protected WaterlilyBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        super.entityInside(param0, param1, param2, param3);
        if (param1 instanceof ServerLevel && param3 instanceof Boat) {
            param1.destroyBlock(new BlockPos(param2), true, param3);
        }

    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return AABB;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        FluidState var0 = param1.getFluidState(param2);
        FluidState var1 = param1.getFluidState(param2.above());
        return (var0.getType() == Fluids.WATER || param0.getMaterial() == Material.ICE) && var1.getType() == Fluids.EMPTY;
    }
}
