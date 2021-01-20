package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PowderSnowBlock extends Block implements BucketPickup {
    public PowderSnowBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
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
        if (!(param3 instanceof LivingEntity) || ((LivingEntity)param3).getFeetBlockState().is(Blocks.POWDER_SNOW)) {
            param3.makeStuckInBlock(param0, new Vec3(0.9F, 0.99F, 0.9F));
        }

        param3.setBodyIsInPowderSnow(true);
        if (!param3.isSpectator() && (param3.xOld != param3.getX() || param3.zOld != param3.getZ()) && param1.random.nextBoolean()) {
            spawnPowderSnowParticles(param1, new Vec3(param3.getX(), (double)param2.getY(), param3.getZ()));
        }

    }

    @Override
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param3 instanceof EntityCollisionContext) {
            EntityCollisionContext var0 = (EntityCollisionContext)param3;
            Optional<Entity> var1 = var0.getEntity();
            if (var1.isPresent() && canEntityWalkOnPowderSnow(var1.get()) && param3.isAbove(Shapes.block(), param2, false) && !param3.isDescending()) {
                return super.getCollisionShape(param0, param1, param2, param3);
            }
        }

        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    public static void spawnPowderSnowParticles(Level param0, Vec3 param1) {
        if (param0.isClientSide) {
            Random var0 = param0.getRandom();
            double var1 = param1.y + 1.0;

            for(int var2 = 0; var2 < var0.nextInt(3); ++var2) {
                param0.addParticle(
                    ParticleTypes.SNOWFLAKE,
                    param1.x,
                    var1,
                    param1.z,
                    (double)((-1.0F + var0.nextFloat() * 2.0F) / 12.0F),
                    0.05F,
                    (double)((-1.0F + var0.nextFloat() * 2.0F) / 12.0F)
                );
            }

        }
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
}
