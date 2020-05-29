package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends Block implements EntityBlock {
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    protected JigsawBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(ORIENTATION);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(ORIENTATION, param1.rotation().rotate(param0.getValue(ORIENTATION)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.setValue(ORIENTATION, param1.rotation().rotate(param0.getValue(ORIENTATION)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getClickedFace();
        Direction var1;
        if (var0.getAxis() == Direction.Axis.Y) {
            var1 = param0.getHorizontalDirection().getOpposite();
        } else {
            var1 = Direction.UP;
        }

        return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(var0, var1));
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
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo param0, StructureTemplate.StructureBlockInfo param1) {
        Direction var0 = getFrontFacing(param0.state);
        Direction var1 = getFrontFacing(param1.state);
        Direction var2 = getTopFacing(param0.state);
        Direction var3 = getTopFacing(param1.state);
        JigsawBlockEntity.JointType var4 = JigsawBlockEntity.JointType.byName(param0.nbt.getString("joint"))
            .orElseGet(() -> var0.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE);
        boolean var5 = var4 == JigsawBlockEntity.JointType.ROLLABLE;
        return var0 == var1.getOpposite() && (var5 || var2 == var3) && param0.nbt.getString("target").equals(param1.nbt.getString("name"));
    }

    public static Direction getFrontFacing(BlockState param0) {
        return param0.getValue(ORIENTATION).front();
    }

    public static Direction getTopFacing(BlockState param0) {
        return param0.getValue(ORIENTATION).top();
    }
}
