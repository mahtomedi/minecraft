package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    protected JukeboxBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param0.getValue(HAS_RECORD)) {
            this.dropRecording(param1, param2);
            param0 = param0.setValue(HAS_RECORD, Boolean.valueOf(false));
            param1.setBlock(param2, param0, 2);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public void setRecord(LevelAccessor param0, BlockPos param1, BlockState param2, ItemStack param3) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof JukeboxBlockEntity) {
            ((JukeboxBlockEntity)var0).setRecord(param3.copy());
            param0.setBlock(param1, param2.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
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
        if (param0.getBlock() != param3.getBlock()) {
            this.dropRecording(param1, param2);
            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new JukeboxBlockEntity();
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
}
