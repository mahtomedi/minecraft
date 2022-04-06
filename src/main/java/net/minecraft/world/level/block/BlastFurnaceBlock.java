package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlastFurnaceBlock extends AbstractFurnaceBlock {
    protected BlastFurnaceBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new BlastFurnaceBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return createFurnaceTicker(param0, param2, BlockEntityType.BLAST_FURNACE);
    }

    @Override
    protected void openContainer(Level param0, BlockPos param1, Player param2) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof BlastFurnaceBlockEntity) {
            param2.openMenu((MenuProvider)var0);
            param2.awardStat(Stats.INTERACT_WITH_BLAST_FURNACE);
        }

    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (param0.getValue(LIT)) {
            double var0 = (double)param2.getX() + 0.5;
            double var1 = (double)param2.getY();
            double var2 = (double)param2.getZ() + 0.5;
            if (param3.nextDouble() < 0.1) {
                param1.playLocalSound(var0, var1, var2, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction var3 = param0.getValue(FACING);
            Direction.Axis var4 = var3.getAxis();
            double var5 = 0.52;
            double var6 = param3.nextDouble() * 0.6 - 0.3;
            double var7 = var4 == Direction.Axis.X ? (double)var3.getStepX() * 0.52 : var6;
            double var8 = param3.nextDouble() * 9.0 / 16.0;
            double var9 = var4 == Direction.Axis.Z ? (double)var3.getStepZ() * 0.52 : var6;
            param1.addParticle(ParticleTypes.SMOKE, var0 + var7, var1 + var8, var2 + var9, 0.0, 0.0, 0.0);
        }
    }
}
