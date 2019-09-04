package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndGatewayBlock extends BaseEntityBlock {
    protected EndGatewayBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new TheEndGatewayBlockEntity();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof TheEndGatewayBlockEntity) {
            int var1 = ((TheEndGatewayBlockEntity)var0).getParticleAmount();

            for(int var2 = 0; var2 < var1; ++var2) {
                double var3 = (double)((float)param2.getX() + param3.nextFloat());
                double var4 = (double)((float)param2.getY() + param3.nextFloat());
                double var5 = (double)((float)param2.getZ() + param3.nextFloat());
                double var6 = ((double)param3.nextFloat() - 0.5) * 0.5;
                double var7 = ((double)param3.nextFloat() - 0.5) * 0.5;
                double var8 = ((double)param3.nextFloat() - 0.5) * 0.5;
                int var9 = param3.nextInt(2) * 2 - 1;
                if (param3.nextBoolean()) {
                    var5 = (double)param2.getZ() + 0.5 + 0.25 * (double)var9;
                    var8 = (double)(param3.nextFloat() * 2.0F * (float)var9);
                } else {
                    var3 = (double)param2.getX() + 0.5 + 0.25 * (double)var9;
                    var6 = (double)(param3.nextFloat() * 2.0F * (float)var9);
                }

                param1.addParticle(ParticleTypes.PORTAL, var3, var4, var5, var6, var7, var8);
            }

        }
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
