package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<SaplingBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(TreeGrower.CODEC.fieldOf("tree").forGetter(param0x -> param0x.treeGrower), propertiesCodec()).apply(param0, SaplingBlock::new)
    );
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    protected final TreeGrower treeGrower;

    @Override
    public MapCodec<? extends SaplingBlock> codec() {
        return CODEC;
    }

    protected SaplingBlock(TreeGrower param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.treeGrower = param0;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param1.getMaxLocalRawBrightness(param2.above()) >= 9 && param3.nextInt(7) == 0) {
            this.advanceTree(param1, param2, param0, param3);
        }

    }

    public void advanceTree(ServerLevel param0, BlockPos param1, BlockState param2, RandomSource param3) {
        if (param2.getValue(STAGE) == 0) {
            param0.setBlock(param1, param2.cycle(STAGE), 4);
        } else {
            this.treeGrower.growTree(param0, param0.getChunkSource().getGenerator(), param1, param2, param3);
        }

    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return (double)param0.random.nextFloat() < 0.45;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        this.advanceTree(param0, param2, param3, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(STAGE);
    }
}
