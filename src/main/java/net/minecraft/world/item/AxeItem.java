package net.minecraft.world.item;

import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class AxeItem extends DiggerItem {
    private static final Set<Material> DIGGABLE_MATERIALS = Sets.newHashSet(
        Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.VEGETABLE
    );
    private static final Set<Block> OTHER_DIGGABLE_BLOCKS = Sets.newHashSet(
        Blocks.LADDER,
        Blocks.SCAFFOLDING,
        Blocks.OAK_BUTTON,
        Blocks.SPRUCE_BUTTON,
        Blocks.BIRCH_BUTTON,
        Blocks.JUNGLE_BUTTON,
        Blocks.DARK_OAK_BUTTON,
        Blocks.ACACIA_BUTTON,
        Blocks.CRIMSON_BUTTON,
        Blocks.WARPED_BUTTON
    );
    protected static final Map<Block, Block> STRIPPABLES = new Builder<Block, Block>()
        .put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
        .put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
        .put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
        .put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
        .put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
        .put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
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
        .build();

    protected AxeItem(Tier param0, float param1, float param2, Item.Properties param3) {
        super(param1, param2, param0, OTHER_DIGGABLE_BLOCKS, param3);
    }

    @Override
    public float getDestroySpeed(ItemStack param0, BlockState param1) {
        Material var0 = param1.getMaterial();
        return DIGGABLE_MATERIALS.contains(var0) ? this.speed : super.getDestroySpeed(param0, param1);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        Player var2 = param0.getPlayer();
        BlockState var3 = var0.getBlockState(var1);
        Optional<BlockState> var4 = this.getStripped(var3);
        Optional<BlockState> var5 = WeatheringCopper.getPrevious(var3);
        Optional<BlockState> var6 = Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(var3.getBlock())).map(param1 -> param1.withPropertiesOf(var3));
        Optional<BlockState> var7 = Optional.empty();
        if (var4.isPresent()) {
            var0.playSound(var2, var1, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            var7 = var4;
        } else if (var5.isPresent()) {
            var0.playSound(var2, var1, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            var0.levelEvent(var2, 3005, var1, 0);
            var7 = var5;
        } else if (var6.isPresent()) {
            var0.playSound(var2, var1, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            var0.levelEvent(var2, 3004, var1, 0);
            var7 = var6;
        }

        if (var7.isPresent()) {
            var0.setBlock(var1, var7.get(), 11);
            if (var2 != null) {
                param0.getItemInHand().hurtAndBreak(1, var2, param1 -> param1.broadcastBreakEvent(param0.getHand()));
            }

            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    private Optional<BlockState> getStripped(BlockState param0) {
        return Optional.ofNullable(STRIPPABLES.get(param0.getBlock()))
            .map(param1 -> param1.defaultBlockState().setValue(RotatedPillarBlock.AXIS, param0.getValue(RotatedPillarBlock.AXIS)));
    }
}
