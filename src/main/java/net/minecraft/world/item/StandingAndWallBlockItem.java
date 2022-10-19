package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StandingAndWallBlockItem extends BlockItem {
    protected final Block wallBlock;
    private final Direction attachmentDirection;

    public StandingAndWallBlockItem(Block param0, Block param1, Item.Properties param2, Direction param3) {
        super(param0, param2);
        this.wallBlock = param1;
        this.attachmentDirection = param3;
    }

    protected boolean canPlace(LevelReader param0, BlockState param1, BlockPos param2) {
        return param1.canSurvive(param0, param2);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext param0) {
        BlockState var0 = this.wallBlock.getStateForPlacement(param0);
        BlockState var1 = null;
        LevelReader var2 = param0.getLevel();
        BlockPos var3 = param0.getClickedPos();

        for(Direction var4 : param0.getNearestLookingDirections()) {
            if (var4 != this.attachmentDirection.getOpposite()) {
                BlockState var5 = var4 == this.attachmentDirection ? this.getBlock().getStateForPlacement(param0) : var0;
                if (var5 != null && this.canPlace(var2, var5, var3)) {
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
