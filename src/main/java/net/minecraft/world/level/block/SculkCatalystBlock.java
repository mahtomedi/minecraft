package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;

public class SculkCatalystBlock extends BaseEntityBlock {
    public static final int PULSE_TICKS = 8;
    public static final BooleanProperty PULSE = BlockStateProperties.BLOOM;
    private final IntProvider xpRange = ConstantInt.of(20);

    public SculkCatalystBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(PULSE, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(PULSE);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param0.getValue(PULSE)) {
            param1.setBlock(param2, param0.setValue(PULSE, Boolean.valueOf(false)), 3);
        }

    }

    public static void bloom(ServerLevel param0, BlockPos param1, BlockState param2, RandomSource param3) {
        param0.setBlock(param1, param2.setValue(PULSE, Boolean.valueOf(true)), 3);
        param0.scheduleTick(param1, param2.getBlock(), 8);
        param0.sendParticles(
            ParticleTypes.SCULK_SOUL, (double)param1.getX() + 0.5, (double)param1.getY() + 1.15, (double)param1.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0
        );
        param0.playSound(null, param1, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + param3.nextFloat() * 0.4F);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new SculkCatalystBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel param0, T param1) {
        return param1 instanceof SculkCatalystBlockEntity ? (SculkCatalystBlockEntity)param1 : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return param0.isClientSide ? null : createTickerHelper(param2, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3, boolean param4) {
        super.spawnAfterBreak(param0, param1, param2, param3, param4);
        if (param4) {
            this.tryDropExperience(param1, param2, param3, this.xpRange);
        }

    }
}
