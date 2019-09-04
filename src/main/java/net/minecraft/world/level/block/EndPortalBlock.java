package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndPortalBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    protected EndPortalBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new TheEndPortalBlockEntity();
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide
            && !param3.isPassenger()
            && !param3.isVehicle()
            && param3.canChangeDimensions()
            && Shapes.joinIsNotEmpty(
                Shapes.create(param3.getBoundingBox().move((double)(-param2.getX()), (double)(-param2.getY()), (double)(-param2.getZ()))),
                param0.getShape(param1, param2),
                BooleanOp.AND
            )) {
            param3.changeDimension(param1.dimension.getType() == DimensionType.THE_END ? DimensionType.OVERWORLD : DimensionType.THE_END);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        double var0 = (double)((float)param2.getX() + param3.nextFloat());
        double var1 = (double)((float)param2.getY() + 0.8F);
        double var2 = (double)((float)param2.getZ() + param3.nextFloat());
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
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
