package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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
import net.minecraft.world.level.block.state.BlockState;

public class HoeItem extends DiggerItem {
    private static final Set<Block> DIGGABLES = ImmutableSet.of(
        Blocks.NETHER_WART_BLOCK,
        Blocks.WARPED_WART_BLOCK,
        Blocks.HAY_BLOCK,
        Blocks.DRIED_KELP_BLOCK,
        Blocks.TARGET,
        Blocks.SHROOMLIGHT,
        Blocks.SPONGE,
        Blocks.WET_SPONGE,
        Blocks.JUNGLE_LEAVES,
        Blocks.OAK_LEAVES,
        Blocks.SPRUCE_LEAVES,
        Blocks.DARK_OAK_LEAVES,
        Blocks.ACACIA_LEAVES,
        Blocks.BIRCH_LEAVES
    );
    protected static final Map<Block, BlockState> TILLABLES = Maps.newHashMap(
        ImmutableMap.of(
            Blocks.GRASS_BLOCK,
            Blocks.FARMLAND.defaultBlockState(),
            Blocks.GRASS_PATH,
            Blocks.FARMLAND.defaultBlockState(),
            Blocks.DIRT,
            Blocks.FARMLAND.defaultBlockState(),
            Blocks.COARSE_DIRT,
            Blocks.DIRT.defaultBlockState()
        )
    );

    protected HoeItem(Tier param0, int param1, float param2, Item.Properties param3) {
        super((float)param1, param2, param0, DIGGABLES, param3);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        if (param0.getClickedFace() != Direction.DOWN && var0.getBlockState(var1.above()).isAir()) {
            BlockState var2 = TILLABLES.get(var0.getBlockState(var1).getBlock());
            if (var2 != null) {
                Player var3 = param0.getPlayer();
                var0.playSound(var3, var1, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!var0.isClientSide) {
                    var0.setBlock(var1, var2, 11);
                    if (var3 != null) {
                        param0.getItemInHand().hurtAndBreak(1, var3, param1 -> param1.broadcastBreakEvent(param0.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(var0.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}
