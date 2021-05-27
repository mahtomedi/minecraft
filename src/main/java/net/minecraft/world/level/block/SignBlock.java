package net.minecraft.world.level.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final float AABB_OFFSET = 4.0F;
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private final WoodType type;

    protected SignBlock(BlockBehaviour.Properties param0, WoodType param1) {
        super(param0);
        this.type = param1;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new SignBlockEntity(param0, param1);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        Item var1 = var0.getItem();
        boolean var2 = var1 instanceof DyeItem;
        boolean var3 = var0.is(Items.GLOW_INK_SAC);
        boolean var4 = var0.is(Items.INK_SAC);
        boolean var5 = (var3 || var2 || var4) && param3.getAbilities().mayBuild;
        if (param1.isClientSide) {
            return var5 ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        } else {
            BlockEntity var6 = param1.getBlockEntity(param2);
            if (!(var6 instanceof SignBlockEntity)) {
                return InteractionResult.PASS;
            } else {
                SignBlockEntity var7 = (SignBlockEntity)var6;
                boolean var8 = var7.hasGlowingText();
                if ((!var3 || !var8) && (!var4 || var8)) {
                    if (var5) {
                        boolean var9;
                        if (var3) {
                            param1.playSound(null, param2, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            var9 = var7.setHasGlowingText(true);
                            if (param3 instanceof ServerPlayer) {
                                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)param3, param2, var0);
                            }
                        } else if (var4) {
                            param1.playSound(null, param2, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            var9 = var7.setHasGlowingText(false);
                        } else {
                            param1.playSound(null, param2, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            var9 = var7.setColor(((DyeItem)var1).getDyeColor());
                        }

                        if (var9) {
                            if (!param3.isCreative()) {
                                var0.shrink(1);
                            }

                            param3.awardStat(Stats.ITEM_USED.get(var1));
                        }
                    }

                    return var7.executeClickCommands((ServerPlayer)param3) ? InteractionResult.SUCCESS : InteractionResult.PASS;
                } else {
                    return InteractionResult.PASS;
                }
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    public WoodType type() {
        return this.type;
    }
}
