package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DaylightDetectorBlock extends BaseEntityBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

    public DaylightDetectorBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)).setValue(INVERTED, Boolean.valueOf(false)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWER);
    }

    public static void updateSignalStrength(BlockState param0, Level param1, BlockPos param2) {
        if (param1.dimension.isHasSkyLight()) {
            int var0 = param1.getBrightness(LightLayer.SKY, param2) - param1.getSkyDarken();
            float var1 = param1.getSunAngle(1.0F);
            boolean var2 = param0.getValue(INVERTED);
            if (var2) {
                var0 = 15 - var0;
            } else if (var0 > 0) {
                float var3 = var1 < (float) Math.PI ? 0.0F : (float) (Math.PI * 2);
                var1 += (var3 - var1) * 0.2F;
                var0 = Math.round((float)var0 * Mth.cos(var1));
            }

            var0 = Mth.clamp(var0, 0, 15);
            if (param0.getValue(POWER) != var0) {
                param1.setBlock(param2, param0.setValue(POWER, Integer.valueOf(var0)), 3);
            }

        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param3.mayBuild()) {
            if (param1.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                BlockState var0 = param0.cycle(INVERTED);
                param1.setBlock(param2, var0, 4);
                updateSignalStrength(var0, param1, param2);
                return InteractionResult.SUCCESS;
            }
        } else {
            return super.use(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new DaylightDetectorBlockEntity();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(POWER, INVERTED);
    }
}
