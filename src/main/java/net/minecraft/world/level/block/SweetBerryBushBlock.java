package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SweetBerryBushBlock extends BushBlock implements BonemealableBlock {
    private static final float HURT_SPEED_THRESHOLD = 0.003F;
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SAPLING_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public SweetBerryBushBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return new ItemStack(Items.SWEET_BERRIES);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param0.getValue(AGE) == 0) {
            return SAPLING_SHAPE;
        } else {
            return param0.getValue(AGE) < 3 ? MID_GROWTH_SHAPE : super.getShape(param0, param1, param2, param3);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getValue(AGE) < 3;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        int var0 = param0.getValue(AGE);
        if (var0 < 3 && param3.nextInt(5) == 0 && param1.getRawBrightness(param2.above(), 0) >= 9) {
            param1.setBlock(param2, param0.setValue(AGE, Integer.valueOf(var0 + 1)), 2);
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param3 instanceof LivingEntity && param3.getType() != EntityType.FOX && param3.getType() != EntityType.BEE) {
            param3.makeStuckInBlock(param0, new Vec3(0.8F, 0.75, 0.8F));
            if (!param1.isClientSide && param0.getValue(AGE) > 0 && (param3.xOld != param3.getX() || param3.zOld != param3.getZ())) {
                double var0 = Math.abs(param3.getX() - param3.xOld);
                double var1 = Math.abs(param3.getZ() - param3.zOld);
                if (var0 >= 0.003F || var1 >= 0.003F) {
                    param3.hurt(DamageSource.SWEET_BERRY_BUSH, 1.0F);
                }
            }

        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        int var0 = param0.getValue(AGE);
        boolean var1 = var0 == 3;
        if (!var1 && param3.getItemInHand(param4).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        } else if (var0 > 1) {
            int var2 = 1 + param1.random.nextInt(2);
            popResource(param1, param2, new ItemStack(Items.SWEET_BERRIES, var2 + (var1 ? 1 : 0)));
            param1.playSound(null, param2, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + param1.random.nextFloat() * 0.4F);
            param1.setBlock(param2, param0.setValue(AGE, Integer.valueOf(1)), 2);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return super.use(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return param2.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        int var0 = Math.min(3, param3.getValue(AGE) + 1);
        param0.setBlock(param2, param3.setValue(AGE, Integer.valueOf(var0)), 2);
    }
}
