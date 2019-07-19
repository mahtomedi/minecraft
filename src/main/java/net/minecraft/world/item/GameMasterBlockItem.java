package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GameMasterBlockItem extends BlockItem {
    public GameMasterBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext param0) {
        Player var0 = param0.getPlayer();
        return var0 != null && !var0.canUseGameMasterBlocks() ? null : super.getPlacementState(param0);
    }
}
