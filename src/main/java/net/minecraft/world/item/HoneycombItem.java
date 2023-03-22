package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoneycombItem extends Item implements SignApplicator {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(
        () -> ImmutableBiMap.<Block, Block>builder()
                .put(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK)
                .put(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER)
                .put(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER)
                .put(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER)
                .put(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
                .put(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
                .put(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
                .put(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
                .put(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB)
                .put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)
                .put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)
                .put(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB)
                .put(Blocks.CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS)
                .put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS)
                .put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)
                .put(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
                .build()
    );
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    public HoneycombItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        return getWaxed(var2).map(param3 -> {
            Player var0x = param0.getPlayer();
            ItemStack var1x = param0.getItemInHand();
            if (var0x instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)var0x, var1, var1x);
            }

            var1x.shrink(1);
            var0.setBlock(var1, param3, 11);
            var0.gameEvent(GameEvent.BLOCK_CHANGE, var1, GameEvent.Context.of(var0x, param3));
            var0.levelEvent(var0x, 3003, var1, 0);
            return InteractionResult.sidedSuccess(var0.isClientSide);
        }).orElse(InteractionResult.PASS);
    }

    public static Optional<BlockState> getWaxed(BlockState param0) {
        return Optional.ofNullable(WAXABLES.get().get(param0.getBlock())).map(param1 -> param1.withPropertiesOf(param0));
    }

    @Override
    public boolean tryApplyToSign(Level param0, SignBlockEntity param1, boolean param2, Player param3) {
        if (param1.setWaxed(true)) {
            param0.levelEvent(null, 3003, param1.getBlockPos(), 0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canApplyToSign(SignText param0, Player param1) {
        return true;
    }
}
