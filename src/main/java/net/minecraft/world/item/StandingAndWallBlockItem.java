package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StandingAndWallBlockItem extends BlockItem {
    protected final Block wallBlock;

    public StandingAndWallBlockItem(Block param0, Block param1, Item.Properties param2) {
        super(param0, param2);
        this.wallBlock = param1;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext param0) {
        BlockState var0 = this.wallBlock.getStateForPlacement(param0);
        BlockState var1 = null;
        LevelReader var2 = param0.getLevel();
        BlockPos var3 = param0.getClickedPos();

        for(Direction var4 : param0.getNearestLookingDirections()) {
            if (var4 != Direction.UP) {
                BlockState var5 = var4 == Direction.DOWN ? this.getBlock().getStateForPlacement(param0) : var0;
                if (var5 != null && var5.canSurvive(var2, var3)) {
                    var1 = var5;
                    break;
                }
            }
        }

        return var1 != null && var2.isUnobstructed(var1, var3, CollisionContext.empty()) ? var1 : null;
    }

    @Override
    public void registerBlocks(Map<Block, Item> param0, Item param1) {
        super.registerBlocks(param0, param1);
        param0.put(this.wallBlock, param1);
    }
}
