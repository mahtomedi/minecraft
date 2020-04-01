package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ZoneBlock extends Block {
    protected ZoneBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.block();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        double var0 = (double)param2.getX() + 0.5;
        double var1 = (double)param2.getZ() + 0.5;

        for(int var2 = 0; var2 < 3; ++var2) {
            if (param3.nextBoolean()) {
                param1.addParticle(
                    ParticleTypes.COMPOSTER,
                    var0 + (double)(param3.nextFloat() / 5.0F),
                    (double)param2.getY() + (0.5 - (double)param3.nextFloat()),
                    var1 + (double)(param3.nextFloat() / 5.0F),
                    0.0,
                    0.0,
                    0.0
                );
            }
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param3 instanceof LivingEntity) {
            ((LivingEntity)param3).addEffect(new MobEffectInstance(MobEffects.POISON, 60, 3, true, true));
            ((LivingEntity)param3).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, true, true));
        }

        if (param3 instanceof ItemEntity) {
            param3.setDeltaMovement(param3.getDeltaMovement().multiply(0.0, 2.0, 0.0));
        }

        super.entityInside(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getShadeBrightness(BlockState param0, BlockGetter param1, BlockPos param2) {
        return 1.0F;
    }
}
