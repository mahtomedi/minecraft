package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class LoomBlock extends HorizontalDirectionalBlock {
    private static final TranslatableComponent CONTAINER_TITLE = new TranslatableComponent("container.loom");

    protected LoomBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return true;
        } else {
            param3.openMenu(param0.getMenuProvider(param1, param2));
            param3.awardStat(Stats.INTERACT_WITH_LOOM);
            return true;
        }
    }

    @Override
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        return new SimpleMenuProvider((param2x, param3, param4) -> new LoomMenu(param2x, param3, ContainerLevelAccess.create(param1, param2)), CONTAINER_TITLE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }
}
