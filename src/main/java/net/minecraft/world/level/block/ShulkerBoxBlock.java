package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShulkerBoxBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    @Nullable
    private final DyeColor color;

    public ShulkerBoxBlock(@Nullable DyeColor param0, Block.Properties param1) {
        super(param1);
        this.color = param0;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new ShulkerBoxBlockEntity(this.color);
    }

    @Override
    public boolean isViewBlocking(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (param3.isSpectator()) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof ShulkerBoxBlockEntity) {
                Direction var1 = param0.getValue(FACING);
                ShulkerBoxBlockEntity var2 = (ShulkerBoxBlockEntity)var0;
                boolean var4;
                if (var2.getAnimationStatus() == ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
                    AABB var3 = Shapes.block()
                        .bounds()
                        .expandTowards(
                            (double)(0.5F * (float)var1.getStepX()), (double)(0.5F * (float)var1.getStepY()), (double)(0.5F * (float)var1.getStepZ())
                        )
                        .contract((double)var1.getStepX(), (double)var1.getStepY(), (double)var1.getStepZ());
                    var4 = param1.noCollision(var3.move(param2.relative(var1)));
                } else {
                    var4 = true;
                }

                if (var4) {
                    param3.openMenu(var2);
                    param3.awardStat(Stats.OPEN_SHULKER_BOX);
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    @Override
    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity var1 = (ShulkerBoxBlockEntity)var0;
            if (!param0.isClientSide && param3.isCreative() && !var1.isEmpty()) {
                ItemStack var2 = getColoredItemStack(this.getColor());
                CompoundTag var3 = var1.saveToTag(new CompoundTag());
                if (!var3.isEmpty()) {
                    var2.addTagElement("BlockEntityTag", var3);
                }

                if (var1.hasCustomName()) {
                    var2.setHoverName(var1.getCustomName());
                }

                ItemEntity var4 = new ItemEntity(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), var2);
                var4.setDefaultPickUpDelay();
                param0.addFreshEntity(var4);
            } else {
                var1.unpackLootTable(param3);
            }
        }

        super.playerWillDestroy(param0, param1, param2, param3);
    }

    @Override
    public List<ItemStack> getDrops(BlockState param0, LootContext.Builder param1) {
        BlockEntity var0 = param1.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (var0 instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity var1 = (ShulkerBoxBlockEntity)var0;
            param1 = param1.withDynamicDrop(CONTENTS, (param1x, param2) -> {
                for(int var0x = 0; var0x < var1.getContainerSize(); ++var0x) {
                    param2.accept(var1.getItem(var0x));
                }

            });
        }

        return super.getDrops(param0, param1);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof ShulkerBoxBlockEntity) {
                ((ShulkerBoxBlockEntity)var0).setCustomName(param4.getHoverName());
            }
        }

    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param0.getBlock() != param3.getBlock()) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof ShulkerBoxBlockEntity) {
                param1.updateNeighbourForOutputSignal(param2, param0.getBlock());
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable BlockGetter param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        CompoundTag var0 = param0.getTagElement("BlockEntityTag");
        if (var0 != null) {
            if (var0.contains("LootTable", 8)) {
                param2.add(new TextComponent("???????"));
            }

            if (var0.contains("Items", 9)) {
                NonNullList<ItemStack> var1 = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(var0, var1);
                int var2 = 0;
                int var3 = 0;

                for(ItemStack var4 : var1) {
                    if (!var4.isEmpty()) {
                        ++var3;
                        if (var2 <= 4) {
                            ++var2;
                            Component var5 = var4.getHoverName().deepCopy();
                            var5.append(" x").append(String.valueOf(var4.getCount()));
                            param2.add(var5);
                        }
                    }
                }

                if (var3 - var2 > 0) {
                    param2.add(new TranslatableComponent("container.shulkerBox.more", var3 - var2).withStyle(ChatFormatting.ITALIC));
                }
            }
        }

    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        return var0 instanceof ShulkerBoxBlockEntity ? Shapes.create(((ShulkerBoxBlockEntity)var0).getBoundingBox(param0)) : Shapes.block();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)param1.getBlockEntity(param2));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        ItemStack var0 = super.getCloneItemStack(param0, param1, param2);
        ShulkerBoxBlockEntity var1 = (ShulkerBoxBlockEntity)param0.getBlockEntity(param1);
        CompoundTag var2 = var1.saveToTag(new CompoundTag());
        if (!var2.isEmpty()) {
            var0.addTagElement("BlockEntityTag", var2);
        }

        return var0;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static DyeColor getColorFromItem(Item param0) {
        return getColorFromBlock(Block.byItem(param0));
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static DyeColor getColorFromBlock(Block param0) {
        return param0 instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)param0).getColor() : null;
    }

    public static Block getBlockByColor(@Nullable DyeColor param0) {
        if (param0 == null) {
            return Blocks.SHULKER_BOX;
        } else {
            switch(param0) {
                case WHITE:
                    return Blocks.WHITE_SHULKER_BOX;
                case ORANGE:
                    return Blocks.ORANGE_SHULKER_BOX;
                case MAGENTA:
                    return Blocks.MAGENTA_SHULKER_BOX;
                case LIGHT_BLUE:
                    return Blocks.LIGHT_BLUE_SHULKER_BOX;
                case YELLOW:
                    return Blocks.YELLOW_SHULKER_BOX;
                case LIME:
                    return Blocks.LIME_SHULKER_BOX;
                case PINK:
                    return Blocks.PINK_SHULKER_BOX;
                case GRAY:
                    return Blocks.GRAY_SHULKER_BOX;
                case LIGHT_GRAY:
                    return Blocks.LIGHT_GRAY_SHULKER_BOX;
                case CYAN:
                    return Blocks.CYAN_SHULKER_BOX;
                case PURPLE:
                default:
                    return Blocks.PURPLE_SHULKER_BOX;
                case BLUE:
                    return Blocks.BLUE_SHULKER_BOX;
                case BROWN:
                    return Blocks.BROWN_SHULKER_BOX;
                case GREEN:
                    return Blocks.GREEN_SHULKER_BOX;
                case RED:
                    return Blocks.RED_SHULKER_BOX;
                case BLACK:
                    return Blocks.BLACK_SHULKER_BOX;
            }
        }
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public static ItemStack getColoredItemStack(@Nullable DyeColor param0) {
        return new ItemStack(getBlockByColor(param0));
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }
}
