package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoeItem extends DiggerItem {
    protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap(
        ImmutableMap.of(
            Blocks.GRASS_BLOCK,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.DIRT_PATH,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.DIRT,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.COARSE_DIRT,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.ROOTED_DIRT,
            Pair.of(param0 -> true, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))
        )
    );

    protected HoeItem(Tier param0, int param1, float param2, Item.Properties param3) {
        super((float)param1, param2, param0, BlockTags.MINEABLE_WITH_HOE, param3);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> var2 = TILLABLES.get(var0.getBlockState(var1).getBlock());
        if (var2 == null) {
            return InteractionResult.PASS;
        } else {
            Predicate<UseOnContext> var3 = var2.getFirst();
            Consumer<UseOnContext> var4 = var2.getSecond();
            if (var3.test(param0)) {
                Player var5 = param0.getPlayer();
                var0.playSound(var5, var1, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!var0.isClientSide) {
                    var4.accept(param0);
                    if (var5 != null) {
                        param0.getItemInHand().hurtAndBreak(1, var5, param1 -> param1.broadcastBreakEvent(param0.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(var0.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static Consumer<UseOnContext> changeIntoState(BlockState param0) {
        return param1 -> {
            param1.getLevel().setBlock(param1.getClickedPos(), param0, 11);
            param1.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, param1.getClickedPos(), GameEvent.Context.of(param1.getPlayer(), param0));
        };
    }

    public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState param0, ItemLike param1) {
        return param2 -> {
            param2.getLevel().setBlock(param2.getClickedPos(), param0, 11);
            param2.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, param2.getClickedPos(), GameEvent.Context.of(param2.getPlayer(), param0));
            Block.popResourceFromFace(param2.getLevel(), param2.getClickedPos(), param2.getClickedFace(), new ItemStack(param1));
        };
    }

    public static boolean onlyIfAirAbove(UseOnContext param0) {
        return param0.getClickedFace() != Direction.DOWN && param0.getLevel().getBlockState(param0.getClickedPos().above()).isAir();
    }
}
