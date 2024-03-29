package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCauldronBlock extends Block {
    private static final int SIDE_THICKNESS = 2;
    private static final int LEG_WIDTH = 4;
    private static final int LEG_HEIGHT = 3;
    private static final int LEG_DEPTH = 2;
    protected static final int FLOOR_LEVEL = 4;
    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape SHAPE = Shapes.join(
        Shapes.block(),
        Shapes.or(box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE),
        BooleanOp.ONLY_FIRST
    );
    protected final CauldronInteraction.InteractionMap interactions;

    @Override
    protected abstract MapCodec<? extends AbstractCauldronBlock> codec();

    public AbstractCauldronBlock(BlockBehaviour.Properties param0, CauldronInteraction.InteractionMap param1) {
        super(param0);
        this.interactions = param1;
    }

    protected double getContentHeight(BlockState param0) {
        return 0.0;
    }

    protected boolean isEntityInsideContent(BlockState param0, BlockPos param1, Entity param2) {
        return param2.getY() < (double)param1.getY() + this.getContentHeight(param0) && param2.getBoundingBox().maxY > (double)param1.getY() + 0.25;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        CauldronInteraction var1 = this.interactions.map().get(var0.getItem());
        return var1.interact(param0, param1, param2, param3, param4, var0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return INSIDE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    public abstract boolean isFull(BlockState var1);

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BlockPos var0 = PointedDripstoneBlock.findStalactiteTipAboveCauldron(param1, param2);
        if (var0 != null) {
            Fluid var1 = PointedDripstoneBlock.getCauldronFillFluidType(param1, var0);
            if (var1 != Fluids.EMPTY && this.canReceiveStalactiteDrip(var1)) {
                this.receiveStalactiteDrip(param0, param1, param2, var1);
            }

        }
    }

    protected boolean canReceiveStalactiteDrip(Fluid param0) {
        return false;
    }

    protected void receiveStalactiteDrip(BlockState param0, Level param1, BlockPos param2, Fluid param3) {
    }
}
