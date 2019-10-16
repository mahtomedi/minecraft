package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends DirectionalBlock implements EntityBlock {
    protected JigsawBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getClickedFace());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new JigsawBlockEntity();
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof JigsawBlockEntity && param3.canUseGameMasterBlocks()) {
            param3.openJigsawBlock((JigsawBlockEntity)var0);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo param0, StructureTemplate.StructureBlockInfo param1) {
        return param0.state.getValue(FACING) == param1.state.getValue(FACING).getOpposite()
            && param0.nbt.getString("attachement_type").equals(param1.nbt.getString("attachement_type"));
    }
}
