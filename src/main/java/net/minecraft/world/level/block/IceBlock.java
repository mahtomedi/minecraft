package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;

public class IceBlock extends HalfTransparentBlock implements Fallable {
    public IceBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, param3, param4, param5);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param5) == 0) {
            if (param0.dimensionType().ultraWarm()) {
                param0.removeBlock(param2, false);
                return;
            }

            Material var0 = param0.getBlockState(param2.below()).getMaterial();
            if (var0.blocksMotion() || var0.isLiquid()) {
                param0.setBlockAndUpdate(param2, Blocks.WATER.defaultBlockState());
            }
        }

    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param1.getBrightness(LightLayer.BLOCK, param2) > 11 - param0.getLightBlock(param1, param2)) {
            this.melt(param0, param1, param2);
        }

    }

    protected void melt(BlockState param0, Level param1, BlockPos param2) {
        if (param1.dimensionType().ultraWarm()) {
            param1.removeBlock(param2, false);
        } else {
            param1.setBlockAndUpdate(param2, Blocks.WATER.defaultBlockState());
            param1.neighborChanged(param2, Blocks.WATER, param2);
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.NORMAL;
    }

    @Override
    public void onLand(Level param0, BlockPos param1, BlockState param2, BlockState param3, FallingBlockEntity param4) {
        if (this.hotBlocksInYourArea(param0, param1)) {
            this.melt(param2, param0, param1);
        }

    }

    private boolean hotBlocksInYourArea(Level param0, BlockPos param1) {
        for(Direction var0 : Direction.values()) {
            BlockState var1 = param0.getBlockState(param1.relative(var0));
            if (this.isHotBlock(var1)) {
                return true;
            }
        }

        return false;
    }

    private boolean isHotBlock(BlockState param0) {
        return param0.is(BlockTags.FIRE) || param0.is(Blocks.LAVA) || param0.is(Blocks.MAGMA_BLOCK);
    }
}
