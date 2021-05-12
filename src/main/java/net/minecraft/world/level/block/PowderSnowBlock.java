package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PowderSnowBlock extends Block implements BucketPickup {
    private static final float HORIZONTAL_PARTICLE_MOMENTUM_FACTOR = 0.083333336F;
    private static final float IN_BLOCK_HORIZONTAL_SPEED_MULTIPLIER = 0.9F;
    private static final float IN_BLOCK_VERTICAL_SPEED_MULTIPLIER = 1.5F;
    private static final float NUM_BLOCKS_TO_FALL_INTO_BLOCK = 2.5F;
    private static final VoxelShape FALLING_COLLISION_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.9F, 1.0);

    public PowderSnowBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        return param1.is(this) ? true : super.skipRendering(param0, param1, param2);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!(param3 instanceof LivingEntity) || param3.getFeetBlockState().is(this)) {
            param3.makeStuckInBlock(param0, new Vec3(0.9F, 1.5, 0.9F));
            if (param1.isClientSide) {
                Random var0 = param1.getRandom();
                boolean var1 = param3.xOld != param3.getX() || param3.zOld != param3.getZ();
                if (var1 && var0.nextBoolean()) {
                    param1.addParticle(
                        ParticleTypes.SNOWFLAKE,
                        param3.getX(),
                        (double)(param2.getY() + 1),
                        param3.getZ(),
                        (double)(Mth.randomBetween(var0, -1.0F, 1.0F) * 0.083333336F),
                        0.05F,
                        (double)(Mth.randomBetween(var0, -1.0F, 1.0F) * 0.083333336F)
                    );
                }
            }
        }

        param3.setIsInPowderSnow(true);
        if (param3.isOnFire()) {
            param1.destroyBlock(param2, false);
        }

        if (!param1.isClientSide) {
            param3.setSharedFlagOnFire(false);
        }

    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param3 instanceof EntityCollisionContext var0) {
            Optional<Entity> var1 = var0.getEntity();
            if (var1.isPresent()) {
                Entity var2 = var1.get();
                if (var2.fallDistance > 2.5F) {
                    return FALLING_COLLISION_SHAPE;
                }

                boolean var3 = var2 instanceof FallingBlockEntity;
                if (var3 || canEntityWalkOnPowderSnow(var2) && param3.isAbove(Shapes.block(), param2, false) && !param3.isDescending()) {
                    return super.getCollisionShape(param0, param1, param2, param3);
                }
            }
        }

        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    public static boolean canEntityWalkOnPowderSnow(Entity param0) {
        if (param0.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            return true;
        } else {
            return param0 instanceof LivingEntity ? ((LivingEntity)param0).getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS) : false;
        }
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, Blocks.AIR.defaultBlockState(), 11);
        if (!param0.isClientSide()) {
            param0.levelEvent(2001, param1, Block.getId(param2));
        }

        return new ItemStack(Items.POWDER_SNOW_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return true;
    }
}
