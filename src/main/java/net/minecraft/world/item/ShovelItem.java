package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ShovelItem extends DiggerItem {
    private static final Set<Block> DIGGABLES = Sets.newHashSet(
        Blocks.CLAY,
        Blocks.DIRT,
        Blocks.COARSE_DIRT,
        Blocks.PODZOL,
        Blocks.FARMLAND,
        Blocks.GRASS_BLOCK,
        Blocks.GRAVEL,
        Blocks.MYCELIUM,
        Blocks.SAND,
        Blocks.RED_SAND,
        Blocks.SNOW_BLOCK,
        Blocks.SNOW,
        Blocks.SOUL_SAND,
        Blocks.GRASS_PATH,
        Blocks.WHITE_CONCRETE_POWDER,
        Blocks.ORANGE_CONCRETE_POWDER,
        Blocks.MAGENTA_CONCRETE_POWDER,
        Blocks.LIGHT_BLUE_CONCRETE_POWDER,
        Blocks.YELLOW_CONCRETE_POWDER,
        Blocks.LIME_CONCRETE_POWDER,
        Blocks.PINK_CONCRETE_POWDER,
        Blocks.GRAY_CONCRETE_POWDER,
        Blocks.LIGHT_GRAY_CONCRETE_POWDER,
        Blocks.CYAN_CONCRETE_POWDER,
        Blocks.PURPLE_CONCRETE_POWDER,
        Blocks.BLUE_CONCRETE_POWDER,
        Blocks.BROWN_CONCRETE_POWDER,
        Blocks.GREEN_CONCRETE_POWDER,
        Blocks.RED_CONCRETE_POWDER,
        Blocks.BLACK_CONCRETE_POWDER,
        Blocks.SOUL_SOIL
    );
    protected static final Map<Block, BlockState> FLATTENABLES = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.GRASS_PATH.defaultBlockState()));

    public ShovelItem(Tier param0, float param1, float param2, Item.Properties param3) {
        super(param1, param2, param0, DIGGABLES, param3);
    }

    @Override
    public boolean canDestroySpecial(BlockState param0) {
        return param0.is(Blocks.SNOW) || param0.is(Blocks.SNOW_BLOCK);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (param0.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        } else {
            Player var3 = param0.getPlayer();
            BlockState var4 = FLATTENABLES.get(var2.getBlock());
            BlockState var5 = null;
            if (var4 != null && var0.getBlockState(var1.above()).isAir()) {
                var0.playSound(var3, var1, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                var5 = var4;
            } else if (var2.getBlock() instanceof CampfireBlock && var2.getValue(CampfireBlock.LIT)) {
                if (!var0.isClientSide()) {
                    var0.levelEvent(null, 1009, var1, 0);
                }

                var5 = var2.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
            }

            if (var5 != null) {
                if (!var0.isClientSide) {
                    var0.setBlock(var1, var5, 11);
                    if (var3 != null) {
                        param0.getItemInHand().hurtAndBreak(1, var3, param1 -> param1.broadcastBreakEvent(param0.getHand()));
                    }
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}
