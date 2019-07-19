package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerWallHeadBlock extends WallSkullBlock {
    protected PlayerWallHeadBlock(Block.Properties param0) {
        super(SkullBlock.Types.PLAYER, param0);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        Blocks.PLAYER_HEAD.setPlacedBy(param0, param1, param2, param3, param4);
    }

    @Override
    public List<ItemStack> getDrops(BlockState param0, LootContext.Builder param1) {
        return Blocks.PLAYER_HEAD.getDrops(param0, param1);
    }
}
