package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WallTorchBlock extends TorchBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.NORTH,
            Block.box(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
            Direction.SOUTH,
            Block.box(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
            Direction.WEST,
            Block.box(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
            Direction.EAST,
            Block.box(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)
        )
    );

    protected WallTorchBlock(BlockBehaviour.Properties param0, ParticleOptions param1) {
        super(param0, param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return getShape(param0);
    }

    public static VoxelShape getShape(BlockState param0) {
        return AABBS.get(param0.getValue(FACING));
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        Direction var0 = param0.getValue(FACING);
        BlockPos var1 = param2.relative(var0.getOpposite());
        BlockState var2 = param1.getBlockState(var1);
        return var2.isFaceSturdy(param1, var1, var0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = this.defaultBlockState();
        LevelReader var1 = param0.getLevel();
        BlockPos var2 = param0.getClickedPos();
        Direction[] var3 = param0.getNearestLookingDirections();

        for(Direction var4 : var3) {
            if (var4.getAxis().isHorizontal()) {
                Direction var5 = var4.getOpposite();
                var0 = var0.setValue(FACING, var5);
                if (var0.canSurvive(var1, var2)) {
                    return var0;
                }
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1.getOpposite() == param0.getValue(FACING) && !param0.canSurvive(param3, param4) ? Blocks.AIR.defaultBlockState() : param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        Direction var0 = param0.getValue(FACING);
        double var1 = (double)param2.getX() + 0.5;
        double var2 = (double)param2.getY() + 0.7;
        double var3 = (double)param2.getZ() + 0.5;
        double var4 = 0.22;
        double var5 = 0.27;
        Direction var6 = var0.getOpposite();
        param1.addParticle(ParticleTypes.SMOKE, var1 + 0.27 * (double)var6.getStepX(), var2 + 0.22, var3 + 0.27 * (double)var6.getStepZ(), 0.0, 0.0, 0.0);
        param1.addParticle(this.flameParticle, var1 + 0.27 * (double)var6.getStepX(), var2 + 0.22, var3 + 0.27 * (double)var6.getStepZ(), 0.0, 0.0, 0.0);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }
}
