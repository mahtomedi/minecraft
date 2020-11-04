package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndPortalBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    protected EndPortalBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new TheEndPortalBlockEntity(param0, param1);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param1 instanceof ServerLevel
            && !param3.isPassenger()
            && !param3.isVehicle()
            && param3.canChangeDimensions()
            && Shapes.joinIsNotEmpty(
                Shapes.create(param3.getBoundingBox().move((double)(-param2.getX()), (double)(-param2.getY()), (double)(-param2.getZ()))),
                param0.getShape(param1, param2),
                BooleanOp.AND
            )) {
            ResourceKey<Level> var0 = param1.dimension() == Level.END ? Level.OVERWORLD : Level.END;
            ServerLevel var1 = ((ServerLevel)param1).getServer().getLevel(var0);
            if (var1 == null) {
                return;
            }

            param3.changeDimension(var1);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        double var0 = (double)param2.getX() + param3.nextDouble();
        double var1 = (double)param2.getY() + 0.8;
        double var2 = (double)param2.getZ() + param3.nextDouble();
        param1.addParticle(ParticleTypes.SMOKE, var0, var1, var2, 0.0, 0.0, 0.0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canBeReplaced(BlockState param0, Fluid param1) {
        return false;
    }
}
