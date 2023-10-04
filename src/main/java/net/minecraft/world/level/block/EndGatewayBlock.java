package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class EndGatewayBlock extends BaseEntityBlock {
    public static final MapCodec<EndGatewayBlock> CODEC = simpleCodec(EndGatewayBlock::new);

    @Override
    public MapCodec<EndGatewayBlock> codec() {
        return CODEC;
    }

    protected EndGatewayBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new TheEndGatewayBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return createTickerHelper(
            param2, BlockEntityType.END_GATEWAY, param0.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::teleportTick
        );
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof TheEndGatewayBlockEntity) {
            int var1 = ((TheEndGatewayBlockEntity)var0).getParticleAmount();

            for(int var2 = 0; var2 < var1; ++var2) {
                double var3 = (double)param2.getX() + param3.nextDouble();
                double var4 = (double)param2.getY() + param3.nextDouble();
                double var5 = (double)param2.getZ() + param3.nextDouble();
                double var6 = (param3.nextDouble() - 0.5) * 0.5;
                double var7 = (param3.nextDouble() - 0.5) * 0.5;
                double var8 = (param3.nextDouble() - 0.5) * 0.5;
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

    @Override
    public ItemStack getCloneItemStack(LevelReader param0, BlockPos param1, BlockState param2) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canBeReplaced(BlockState param0, Fluid param1) {
        return false;
    }
}
