package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class IceBlock extends HalfTransparentBlock {
    public static final MapCodec<IceBlock> CODEC = simpleCodec(IceBlock::new);

    @Override
    public MapCodec<? extends IceBlock> codec() {
        return CODEC;
    }

    public IceBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    public static BlockState meltsInto() {
        return Blocks.WATER.defaultBlockState();
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, param3, param4, param5);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param5) == 0) {
            if (param0.dimensionType().ultraWarm()) {
                param0.removeBlock(param2, false);
                return;
            }

            BlockState var0 = param0.getBlockState(param2.below());
            if (var0.blocksMotion() || var0.liquid()) {
                param0.setBlockAndUpdate(param2, meltsInto());
            }
        }

    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param1.getBrightness(LightLayer.BLOCK, param2) > 11 - param0.getLightBlock(param1, param2)) {
            this.melt(param0, param1, param2);
        }

    }

    protected void melt(BlockState param0, Level param1, BlockPos param2) {
        if (param1.dimensionType().ultraWarm()) {
            param1.removeBlock(param2, false);
        } else {
            param1.setBlockAndUpdate(param2, meltsInto());
            param1.neighborChanged(param2, meltsInto().getBlock(), param2);
        }
    }
}
