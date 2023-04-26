package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnifferEggBlock extends Block {
    public static final int MAX_HATCH_LEVEL = 2;
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    private static final int REGULAR_HATCH_TIME_TICKS = 24000;
    private static final int BOOSTED_HATCH_TIME_TICKS = 12000;
    private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 2.0, 15.0, 16.0, 14.0);

    public SnifferEggBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HATCH);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    public int getHatchLevel(BlockState param0) {
        return param0.getValue(HATCH);
    }

    private boolean isReadyToHatch(BlockState param0) {
        return this.getHatchLevel(param0) == 2;
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!this.isReadyToHatch(param0)) {
            param1.playSound(null, param2, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + param3.nextFloat() * 0.2F);
            param1.setBlock(param2, param0.setValue(HATCH, Integer.valueOf(this.getHatchLevel(param0) + 1)), 2);
        } else {
            param1.playSound(null, param2, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + param3.nextFloat() * 0.2F);
            param1.destroyBlock(param2, false);
            Sniffer var0 = EntityType.SNIFFER.create(param1);
            if (var0 != null) {
                Vec3 var1 = param2.getCenter();
                var0.setBaby(true);
                var0.moveTo(var1.x(), var1.y(), var1.z(), Mth.wrapDegrees(param1.random.nextFloat() * 360.0F), 0.0F);
                param1.addFreshEntity(var0);
            }

        }
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        boolean var0 = hatchBoost(param1, param2);
        if (!param1.isClientSide() && var0) {
            param1.levelEvent(3009, param2, 0);
        }

        int var1 = var0 ? 12000 : 24000;
        int var2 = var1 / 3;
        param1.gameEvent(GameEvent.BLOCK_PLACE, param2, GameEvent.Context.of(param0));
        param1.scheduleTick(param2, this, var2 + param1.random.nextInt(300));
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    public static boolean hatchBoost(BlockGetter param0, BlockPos param1) {
        return param0.getBlockState(param1.below()).is(BlockTags.SNIFFER_EGG_HATCH_BOOST);
    }
}
