package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class NoteBlock extends Block {
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

    public NoteBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(INSTRUMENT, NoteBlockInstrument.HARP)
                .setValue(NOTE, Integer.valueOf(0))
                .setValue(POWERED, Boolean.valueOf(false))
        );
    }

    private static boolean isFeatureFlagEnabled(LevelAccessor param0) {
        return param0.enabledFeatures().contains(FeatureFlags.UPDATE_1_20);
    }

    private BlockState setInstrument(LevelAccessor param0, BlockPos param1, BlockState param2) {
        if (isFeatureFlagEnabled(param0)) {
            BlockState var0 = param0.getBlockState(param1.above());
            return param2.setValue(
                INSTRUMENT, NoteBlockInstrument.byStateAbove(var0).orElseGet(() -> NoteBlockInstrument.byStateBelow(param0.getBlockState(param1.below())))
            );
        } else {
            return param2.setValue(INSTRUMENT, NoteBlockInstrument.byStateBelow(param0.getBlockState(param1.below())));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.setInstrument(param0.getLevel(), param0.getClickedPos(), this.defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        boolean var0 = isFeatureFlagEnabled(param3) ? param1.getAxis() == Direction.Axis.Y : param1 == Direction.DOWN;
        return var0 ? this.setInstrument(param3, param4, param0) : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        boolean var0 = param1.hasNeighborSignal(param2);
        if (var0 != param0.getValue(POWERED)) {
            if (var0) {
                this.playNote(null, param0, param1, param2);
            }

            param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(var0)), 3);
        }

    }

    private void playNote(@Nullable Entity param0, BlockState param1, Level param2, BlockPos param3) {
        if (!param1.getValue(INSTRUMENT).requiresAirAbove() || param2.getBlockState(param3.above()).isAir()) {
            param2.blockEvent(param3, this, 0, 0);
            param2.gameEvent(param0, GameEvent.NOTE_BLOCK_PLAY, param3);
        }

    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            param0 = param0.cycle(NOTE);
            param1.setBlock(param2, param0, 3);
            this.playNote(param3, param0, param1, param2);
            param3.awardStat(Stats.TUNE_NOTEBLOCK);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void attack(BlockState param0, Level param1, BlockPos param2, Player param3) {
        if (!param1.isClientSide) {
            this.playNote(param3, param0, param1, param2);
            param3.awardStat(Stats.PLAY_NOTEBLOCK);
        }
    }

    @Override
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        NoteBlockInstrument var0 = param0.getValue(INSTRUMENT);
        float var2;
        if (var0.isTunable()) {
            int var1 = param0.getValue(NOTE);
            var2 = (float)Math.pow(2.0, (double)(var1 - 12) / 12.0);
            param1.addParticle(
                ParticleTypes.NOTE, (double)param2.getX() + 0.5, (double)param2.getY() + 1.2, (double)param2.getZ() + 0.5, (double)var1 / 24.0, 0.0, 0.0
            );
        } else {
            var2 = 1.0F;
        }

        SoundEvent var4;
        if (var0.hasCustomSound()) {
            var4 = this.getCustomSoundEvent(var0, param1, param2);
            if (var4 == null) {
                return false;
            }
        } else {
            var4 = var0.getSoundEvent();
        }

        param1.playSound(null, param2, var4, SoundSource.RECORDS, 3.0F, var2);
        return true;
    }

    @Nullable
    private SoundEvent getCustomSoundEvent(NoteBlockInstrument param0, Level param1, BlockPos param2) {
        BlockEntity var1 = param1.getBlockEntity(param2.above());
        if (var1 instanceof SkullBlockEntity var0) {
            ResourceLocation var1x = var0.getNoteBlockSound();
            if (var1x != null) {
                return new SoundEvent(var1x);
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(INSTRUMENT, POWERED, NOTE);
    }
}
