package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;

public class NoteBlock extends Block {
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

    public NoteBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(INSTRUMENT, NoteBlockInstrument.HARP)
                .setValue(NOTE, Integer.valueOf(0))
                .setValue(POWERED, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(INSTRUMENT, NoteBlockInstrument.byState(param0.getLevel().getBlockState(param0.getClickedPos().below())));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.DOWN
            ? param0.setValue(INSTRUMENT, NoteBlockInstrument.byState(param2))
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2);
        if (var0 != param0.getValue(POWERED)) {
            if (var0) {
                this.playNote(param1, param2);
            }

            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)), 3);
        }

    }

    private void playNote(Level param0, BlockPos param1) {
        if (param0.getBlockState(param1.above()).isAir()) {
            param0.blockEvent(param1, this, 0, 0);
        }

    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return true;
        } else {
            param0 = param0.cycle(NOTE);
            param1.setBlock(param2, param0, 3);
            this.playNote(param1, param2);
            param3.awardStat(Stats.TUNE_NOTEBLOCK);
            return true;
        }
    }

    @Override
    public void attack(BlockState param0, Level param1, BlockPos param2, Player param3) {
        if (!param1.isClientSide) {
            this.playNote(param1, param2);
            param3.awardStat(Stats.PLAY_NOTEBLOCK);
        }
    }

    @Override
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        int var0 = param0.getValue(NOTE);
        float var1 = (float)Math.pow(2.0, (double)(var0 - 12) / 12.0);
        param1.playSound(null, param2, param0.getValue(INSTRUMENT).getSoundEvent(), SoundSource.RECORDS, 3.0F, var1);
        param1.addParticle(
            ParticleTypes.NOTE, (double)param2.getX() + 0.5, (double)param2.getY() + 1.2, (double)param2.getZ() + 0.5, (double)var0 / 24.0, 0.0, 0.0
        );
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(INSTRUMENT, POWERED, NOTE);
    }
}
