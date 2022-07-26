package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    protected JukeboxBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        super.setPlacedBy(param0, param1, param2, param3, param4);
        CompoundTag var0 = BlockItem.getBlockEntityData(param4);
        if (var0 != null && var0.contains("RecordItem")) {
            param0.setBlock(param1, param2.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
        }

    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param0.getValue(HAS_RECORD)) {
            this.dropRecording(param1, param2);
            param0 = param0.setValue(HAS_RECORD, Boolean.valueOf(false));
            param1.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, param2, GameEvent.Context.of(param0));
            param1.setBlock(param2, param0, 2);
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(param3, param0));
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public void setRecord(@Nullable Entity param0, LevelAccessor param1, BlockPos param2, BlockState param3, ItemStack param4) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof JukeboxBlockEntity var1) {
            var1.setRecord(param4.copy());
            var1.playRecord();
            param1.setBlock(param2, param3.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(param0, param3));
        }

    }

    private void dropRecording(Level param0, BlockPos param1) {
        if (!param0.isClientSide) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity var1 = (JukeboxBlockEntity)var0;
                ItemStack var2 = var1.getRecord();
                if (!var2.isEmpty()) {
                    param0.levelEvent(1010, param1, 0);
                    var1.clearContent();
                    float var3 = 0.7F;
                    double var4 = (double)(param0.random.nextFloat() * 0.7F) + 0.15F;
                    double var5 = (double)(param0.random.nextFloat() * 0.7F) + 0.060000002F + 0.6;
                    double var6 = (double)(param0.random.nextFloat() * 0.7F) + 0.15F;
                    ItemStack var7 = var2.copy();
                    ItemEntity var8 = new ItemEntity(param0, (double)param1.getX() + var4, (double)param1.getY() + var5, (double)param1.getZ() + var6, var7);
                    var8.setDefaultPickUpDelay();
                    param0.addFreshEntity(var8);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            this.dropRecording(param1, param2);
            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new JukeboxBlockEntity(param0, param1);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof JukeboxBlockEntity) {
            Item var1 = ((JukeboxBlockEntity)var0).getRecord().getItem();
            if (var1 instanceof RecordItem) {
                return ((RecordItem)var1).getAnalogOutput();
            }
        }

        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HAS_RECORD);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return param1.getValue(HAS_RECORD) ? createTickerHelper(param2, BlockEntityType.JUKEBOX, JukeboxBlockEntity::playRecordTick) : null;
    }
}
