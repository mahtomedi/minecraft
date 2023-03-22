package net.minecraft.world.level.block;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
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
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
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
        Item var5 = var0.getItem();
        SignApplicator var3 = var5 instanceof SignApplicator var2 ? var2 : null;
        boolean var4 = var3 != null && param3.getAbilities().mayBuild;
        if (param1.isClientSide) {
            return var4 ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        } else {
            BlockEntity var6 = param1.getBlockEntity(param2);
            if (var6 instanceof SignBlockEntity var5) {
                boolean var6x = var5.isFacingFrontText(param3);
                SignText var7 = var5.getText(var6x);
                if (var5.isWaxed()) {
                    boolean var8 = var7.executeClickCommandsIfPresent((ServerPlayer)param3, (ServerLevel)param1, param2);
                    if (!var8) {
                        param1.playSound(null, var5.getBlockPos(), SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
                    }

                    return InteractionResult.SUCCESS;
                } else if (var4
                    && !this.otherPlayerIsEditingSign(param3, var5)
                    && var3.canApplyToSign(var7, param3)
                    && var3.tryApplyToSign(param1, var5, var6x, param3)) {
                    if (!param3.isCreative()) {
                        var0.shrink(1);
                    }

                    if (param3 instanceof ServerPlayer var9) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(var9, param2, var0);
                    }

                    param1.gameEvent(GameEvent.BLOCK_CHANGE, var5.getBlockPos(), GameEvent.Context.of(param3, var5.getBlockState()));
                    param3.awardStat(Stats.ITEM_USED.get(var1));
                    return InteractionResult.SUCCESS;
                } else if (!this.otherPlayerIsEditingSign(param3, var5)) {
                    this.openTextEdit(param3, var5, var6x);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public abstract float getYRotationDegrees(BlockState var1);

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    public WoodType type() {
        return this.type;
    }

    public static WoodType getWoodType(Block param0) {
        WoodType var0;
        if (param0 instanceof SignBlock) {
            var0 = ((SignBlock)param0).type();
        } else {
            var0 = WoodType.OAK;
        }

        return var0;
    }

    public void openTextEdit(Player param0, SignBlockEntity param1, boolean param2) {
        param1.setAllowedPlayerEditor(param0.getUUID());
        param0.openTextEdit(param1, param2);
    }

    private boolean otherPlayerIsEditingSign(Player param0, SignBlockEntity param1) {
        UUID var0 = param1.getPlayerWhoMayEdit();
        return var0 != null && !var0.equals(param0.getUUID());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return createTickerHelper(param2, BlockEntityType.SIGN, SignBlockEntity::tick);
    }
}
