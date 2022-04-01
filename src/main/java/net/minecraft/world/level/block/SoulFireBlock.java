package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SoulFireBlock extends BaseFireBlock {
    public static final int MAX_AGE = 25;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    private final Supplier<BiMap<Block, Tuple<Double, Block>>> fireInteractions = Suppliers.memoize(
        () -> ImmutableBiMap.<Block, Tuple<Double, Block>>builder()
                .put(Blocks.IRON_ORE, new Tuple<>(0.1, Blocks.IRON_BLOCK))
                .put(Blocks.FIRE, new Tuple<>(0.5, Blocks.SOUL_FIRE))
                .build()
    );

    public SoulFireBlock(BlockBehaviour.Properties param0) {
        super(param0, 2.0F);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return this.canSurvive(param0, param3, param4) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        param1.scheduleTick(param2, this, getFireTickDelay(param1.random));
        int var0 = param0.getValue(AGE);
        if (param3.nextFloat() < 0.2F + (float)var0 * 0.03F) {
            param1.removeBlock(param2, false);
        } else {
            int var1 = Math.min(25, var0 + param3.nextInt(3) / 2);
            if (var0 != var1) {
                param0 = param0.setValue(AGE, Integer.valueOf(var1));
                param1.setBlock(param2, param0, 4);

                for(Direction var2 : Direction.values()) {
                    BlockPos var3 = param2.relative(var2);
                    Block var4 = param1.getBlockState(var3).getBlock();
                    if (this.fireInteractions.get().containsKey(var4)) {
                        Tuple<Double, Block> var5 = this.fireInteractions.get().get(var4);
                        if (param1.random.nextDouble() > var5.getA()) {
                            param1.setBlock(var3, var5.getB().defaultBlockState(), 3);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        super.onPlace(param0, param1, param2, param3, param4);
        param1.scheduleTick(param2, this, getFireTickDelay(param1.random));
    }

    private static int getFireTickDelay(Random param0) {
        return 30 + param0.nextInt(10);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return canSurviveOnBlock(param1.getBlockState(param2.below()));
    }

    public static boolean canSurviveOnBlock(BlockState param0) {
        return true;
    }

    @Override
    protected boolean canBurn(BlockState param0) {
        return true;
    }
}
