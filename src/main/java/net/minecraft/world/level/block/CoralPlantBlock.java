package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CoralPlantBlock extends BaseCoralPlantTypeBlock {
    public static final MapCodec<CoralPlantBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(CoralBlock.DEAD_CORAL_FIELD.forGetter(param0x -> param0x.deadBlock), propertiesCodec()).apply(param0, CoralPlantBlock::new)
    );
    private final Block deadBlock;
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0);

    @Override
    public MapCodec<CoralPlantBlock> codec() {
        return CODEC;
    }

    protected CoralPlantBlock(Block param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.deadBlock = param0;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        this.tryScheduleDieTick(param0, param1, param2);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!scanForWater(param0, param1, param2)) {
            param1.setBlock(param2, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)), 2);
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.DOWN && !param0.canSurvive(param3, param4)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            this.tryScheduleDieTick(param0, param3, param4);
            if (param0.getValue(WATERLOGGED)) {
                param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }
}
