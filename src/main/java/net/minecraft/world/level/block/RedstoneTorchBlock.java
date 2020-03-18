package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneTorchBlock extends TorchBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES = new WeakHashMap<>();

    protected RedstoneTorchBlock(BlockBehaviour.Properties param0) {
        super(param0, DustParticleOptions.REDSTONE);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)));
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        for(Direction var0 : Direction.values()) {
            param1.updateNeighborsAt(param2.relative(var0), this);
        }

    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4) {
            for(Direction var0 : Direction.values()) {
                param1.updateNeighborsAt(param2.relative(var0), this);
            }

        }
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(LIT) && Direction.UP != param3 ? 15 : 0;
    }

    protected boolean hasNeighborSignal(Level param0, BlockPos param1, BlockState param2) {
        return param0.hasSignal(param1.below(), Direction.DOWN);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        boolean var0 = this.hasNeighborSignal(param1, param2, param0);
        List<RedstoneTorchBlock.Toggle> var1 = RECENT_TOGGLES.get(param1);

        while(var1 != null && !var1.isEmpty() && param1.getGameTime() - var1.get(0).when > 60L) {
            var1.remove(0);
        }

        if (param0.getValue(LIT)) {
            if (var0) {
                param1.setBlock(param2, param0.setValue(LIT, Boolean.valueOf(false)), 3);
                if (isToggledTooFrequently(param1, param2, true)) {
                    param1.levelEvent(1502, param2, 0);
                    param1.getBlockTicks().scheduleTick(param2, param1.getBlockState(param2).getBlock(), 160);
                }
            }
        } else if (!var0 && !isToggledTooFrequently(param1, param2, false)) {
            param1.setBlock(param2, param0.setValue(LIT, Boolean.valueOf(true)), 3);
        }

    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (param0.getValue(LIT) == this.hasNeighborSignal(param1, param2, param0) && !param1.getBlockTicks().willTickThisTick(param2, this)) {
            param1.getBlockTicks().scheduleTick(param2, this, 2);
        }

    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param3 == Direction.DOWN ? param0.getSignal(param1, param2, param3) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(LIT)) {
            double var0 = (double)param2.getX() + 0.5 + (param3.nextDouble() - 0.5) * 0.2;
            double var1 = (double)param2.getY() + 0.7 + (param3.nextDouble() - 0.5) * 0.2;
            double var2 = (double)param2.getZ() + 0.5 + (param3.nextDouble() - 0.5) * 0.2;
            param1.addParticle(this.flameParticle, var0, var1, var2, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LIT);
    }

    private static boolean isToggledTooFrequently(Level param0, BlockPos param1, boolean param2) {
        List<RedstoneTorchBlock.Toggle> var0 = RECENT_TOGGLES.computeIfAbsent(param0, param0x -> Lists.newArrayList());
        if (param2) {
            var0.add(new RedstoneTorchBlock.Toggle(param1.immutable(), param0.getGameTime()));
        }

        int var1 = 0;

        for(int var2 = 0; var2 < var0.size(); ++var2) {
            RedstoneTorchBlock.Toggle var3 = var0.get(var2);
            if (var3.pos.equals(param1)) {
                if (++var1 >= 8) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class Toggle {
        private final BlockPos pos;
        private final long when;

        public Toggle(BlockPos param0, long param1) {
            this.pos = param0;
            this.when = param1;
        }
    }
}
