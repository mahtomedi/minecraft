package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GoldBlock extends Block {
    public GoldBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        super.playerWillDestroy(param0, param1, param2, param3);
        PiglinAi.angerNearbyPiglinsThatSee(param3);
    }
}
