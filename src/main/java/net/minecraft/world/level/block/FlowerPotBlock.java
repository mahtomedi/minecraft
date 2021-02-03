package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlowerPotBlock extends Block {
    private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
    private final Block content;

    public FlowerPotBlock(Block param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.content = param0;
        POTTED_BY_CONTENT.put(param0, this);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        Item var1 = var0.getItem();
        BlockState var2 = (var1 instanceof BlockItem ? POTTED_BY_CONTENT.getOrDefault(((BlockItem)var1).getBlock(), Blocks.AIR) : Blocks.AIR)
            .defaultBlockState();
        boolean var3 = var2.is(Blocks.AIR);
        boolean var4 = this.isEmpty();
        if (var3 != var4) {
            if (var4) {
                param1.setBlock(param2, var2, 3);
                param3.awardStat(Stats.POT_FLOWER);
                if (!param3.getAbilities().instabuild) {
                    var0.shrink(1);
                }
            } else {
                ItemStack var5 = new ItemStack(this.content);
                if (var0.isEmpty()) {
                    param3.setItemInHand(param4, var5);
                } else if (!param3.addItem(var5)) {
                    param3.drop(var5, false);
                }

                param1.setBlock(param2, Blocks.FLOWER_POT.defaultBlockState(), 3);
            }

            param1.gameEvent(param3, GameEvent.BLOCK_CHANGE, param2);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return InteractionResult.CONSUME;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return this.isEmpty() ? super.getCloneItemStack(param0, param1, param2) : new ItemStack(this.content);
    }

    private boolean isEmpty() {
        return this.content == Blocks.AIR;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.DOWN && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    public Block getContent() {
        return this.content;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
