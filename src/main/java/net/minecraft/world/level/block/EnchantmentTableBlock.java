package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
        .filter(param0 -> Math.abs(param0.getX()) == 2 || Math.abs(param0.getZ()) == 2)
        .map(BlockPos::immutable)
        .toList();

    protected EnchantmentTableBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    public static boolean isValidBookShelf(Level param0, BlockPos param1, BlockPos param2) {
        return param0.getBlockState(param1.offset(param2)).is(Blocks.BOOKSHELF)
            && param0.isEmptyBlock(param1.offset(param2.getX() / 2, param2.getY(), param2.getZ() / 2));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        super.animateTick(param0, param1, param2, param3);

        for(BlockPos var0 : BOOKSHELF_OFFSETS) {
            if (param3.nextInt(16) == 0 && isValidBookShelf(param1, param2, var0)) {
                param1.addParticle(
                    ParticleTypes.ENCHANT,
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 2.0,
                    (double)param2.getZ() + 0.5,
                    (double)((float)var0.getX() + param3.nextFloat()) - 0.5,
                    (double)((float)var0.getY() - param3.nextFloat() - 1.0F),
                    (double)((float)var0.getZ() + param3.nextFloat()) - 0.5
                );
            }
        }

    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new EnchantmentTableBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return param0.isClientSide ? createTickerHelper(param2, BlockEntityType.ENCHANTING_TABLE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            param3.openMenu(param0.getMenuProvider(param1, param2));
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof EnchantmentTableBlockEntity) {
            Component var1 = ((Nameable)var0).getDisplayName();
            return new SimpleMenuProvider((param2x, param3, param4) -> new EnchantmentMenu(param2x, param3, ContainerLevelAccess.create(param1, param2)), var1);
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof EnchantmentTableBlockEntity) {
                ((EnchantmentTableBlockEntity)var0).setCustomName(param4.getHoverName());
            }
        }

    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
