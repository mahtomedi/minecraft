package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.BlockHitResult;

public class StructureBlock extends BaseEntityBlock {
    public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

    protected StructureBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new StructureBlockEntity();
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof StructureBlockEntity) {
            return ((StructureBlockEntity)var0).usedBy(param3) ? InteractionResult.sidedSuccess(param1.isClientSide) : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        if (!param0.isClientSide) {
            if (param3 != null) {
                BlockEntity var0 = param0.getBlockEntity(param1);
                if (var0 instanceof StructureBlockEntity) {
                    ((StructureBlockEntity)var0).createdBy(param3);
                }
            }

        }
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(MODE, StructureMode.DATA);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(MODE);
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (param1 instanceof ServerLevel) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof StructureBlockEntity) {
                StructureBlockEntity var1 = (StructureBlockEntity)var0;
                boolean var2 = param1.hasNeighborSignal(param2);
                boolean var3 = var1.isPowered();
                if (var2 && !var3) {
                    var1.setPowered(true);
                    this.trigger((ServerLevel)param1, var1);
                } else if (!var2 && var3) {
                    var1.setPowered(false);
                }

            }
        }
    }

    private void trigger(ServerLevel param0, StructureBlockEntity param1) {
        switch(param1.getMode()) {
            case SAVE:
                param1.saveStructure(false);
                break;
            case LOAD:
                param1.loadStructure(param0, false);
                break;
            case CORNER:
                param1.unloadStructure();
            case DATA:
        }

    }
}
