package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
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
        boolean var1 = var0.getItem() instanceof DyeItem;
        boolean var2 = var0.is(Items.GLOW_INK_SAC);
        boolean var3 = var0.is(Items.INK_SAC);
        boolean var4 = (var2 || var1 || var3) && param3.getAbilities().mayBuild;
        boolean var5 = param0.getValue(LIT);
        if ((!var2 || !var5) && (!var3 || var5)) {
            if (param1.isClientSide) {
                return var4 ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            } else {
                BlockEntity var6 = param1.getBlockEntity(param2);
                if (var6 instanceof SignBlockEntity) {
                    SignBlockEntity var7 = (SignBlockEntity)var6;
                    if (var4) {
                        boolean var8;
                        if (var2) {
                            param1.playSound(null, param2, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            param1.setBlockAndUpdate(param2, param0.setValue(LIT, Boolean.valueOf(true)));
                            var8 = true;
                        } else if (var3) {
                            param1.playSound(null, param2, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            param1.setBlockAndUpdate(param2, param0.setValue(LIT, Boolean.valueOf(false)));
                            var8 = true;
                        } else {
                            param1.playSound(null, param2, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            var8 = var7.setColor(((DyeItem)var0.getItem()).getDyeColor());
                        }

                        if (var8 && !param3.isCreative()) {
                            var0.shrink(1);
                        }
                    }

                    return var7.executeClickCommands((ServerPlayer)param3) ? InteractionResult.SUCCESS : InteractionResult.PASS;
                } else {
                    return InteractionResult.PASS;
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public WoodType type() {
        return this.type;
    }
}
