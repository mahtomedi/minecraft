package net.minecraft.world.item;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShovelItem extends DiggerItem {
    protected static final Map<Block, BlockState> FLATTENABLES = Maps.newHashMap(
        new Builder()
            .put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState())
            .put(Blocks.DIRT, Blocks.DIRT_PATH.defaultBlockState())
            .put(Blocks.PODZOL, Blocks.DIRT_PATH.defaultBlockState())
            .put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.defaultBlockState())
            .put(Blocks.MYCELIUM, Blocks.DIRT_PATH.defaultBlockState())
            .put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.defaultBlockState())
            .build()
    );

    public ShovelItem(Tier param0, float param1, float param2, Item.Properties param3) {
        super(param1, param2, param0, BlockTags.MINEABLE_WITH_SHOVEL, param3);
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

                CampfireBlock.dowse(param0.getPlayer(), var0, var1, var2);
                var5 = var2.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
            }

            if (var5 != null) {
                if (!var0.isClientSide) {
                    var0.setBlock(var1, var5, 11);
                    var0.gameEvent(GameEvent.BLOCK_CHANGE, var1, GameEvent.Context.of(var3, var5));
                    if (var3 != null) {
                        param0.getItemInHand().hurtAndBreak(1, var3, param1 -> param1.broadcastBreakEvent(param0.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(var0.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}
