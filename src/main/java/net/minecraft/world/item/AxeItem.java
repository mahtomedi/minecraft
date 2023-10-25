package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class AxeItem extends DiggerItem {
    protected static final Map<Block, Block> STRIPPABLES = new Builder<Block, Block>()
        .put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
        .put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
        .put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
        .put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
        .put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
        .put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
        .put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD)
        .put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG)
        .put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD)
        .put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
        .put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD)
        .put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
        .put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD)
        .put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
        .put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)
        .put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
        .put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
        .put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE)
        .put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD)
        .put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG)
        .put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK)
        .build();

    protected AxeItem(Tier param0, float param1, float param2, Item.Properties param3) {
        super(param1, param2, param0, BlockTags.MINEABLE_WITH_AXE, param3);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        Player var2 = param0.getPlayer();
        Optional<BlockState> var3 = this.evaluateNewBlockState(var0, var1, var2, var0.getBlockState(var1));
        if (var3.isEmpty()) {
            return InteractionResult.PASS;
        } else {
            ItemStack var4 = param0.getItemInHand();
            if (var2 instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)var2, var1, var4);
            }

            var0.setBlock(var1, var3.get(), 11);
            var0.gameEvent(GameEvent.BLOCK_CHANGE, var1, GameEvent.Context.of(var2, var3.get()));
            if (var2 != null) {
                var4.hurtAndBreak(1, var2, param1 -> param1.broadcastBreakEvent(param0.getHand()));
            }

            return InteractionResult.sidedSuccess(var0.isClientSide);
        }
    }

    private Optional<BlockState> evaluateNewBlockState(Level param0, BlockPos param1, @Nullable Player param2, BlockState param3) {
        Optional<BlockState> var0 = this.getStripped(param3);
        if (var0.isPresent()) {
            param0.playSound(param2, param1, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return var0;
        } else {
            Optional<BlockState> var1 = WeatheringCopper.getPrevious(param3);
            if (var1.isPresent()) {
                param0.playSound(param2, param1, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                param0.levelEvent(param2, 3005, param1, 0);
                return var1;
            } else {
                Optional<BlockState> var2 = Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(param3.getBlock()))
                    .map(param1x -> param1x.withPropertiesOf(param3));
                if (var2.isPresent()) {
                    param0.playSound(param2, param1, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                    param0.levelEvent(param2, 3004, param1, 0);
                    return var2;
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private Optional<BlockState> getStripped(BlockState param0) {
        return Optional.ofNullable(STRIPPABLES.get(param0.getBlock()))
            .map(param1 -> param1.defaultBlockState().setValue(RotatedPillarBlock.AXIS, param0.getValue(RotatedPillarBlock.AXIS)));
    }
}
