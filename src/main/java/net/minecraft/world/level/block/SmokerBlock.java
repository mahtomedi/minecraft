package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SmokerBlock extends AbstractFurnaceBlock {
    protected SmokerBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new SmokerBlockEntity();
    }

    @Override
    protected void openContainer(Level param0, BlockPos param1, Player param2) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof SmokerBlockEntity) {
            param2.openMenu((MenuProvider)var0);
            param2.awardStat(Stats.INTERACT_WITH_SMOKER);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(LIT)) {
            double var0 = (double)param2.getX() + 0.5;
            double var1 = (double)param2.getY();
            double var2 = (double)param2.getZ() + 0.5;
            if (param3.nextDouble() < 0.1) {
                param1.playLocalSound(var0, var1, var2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            param1.addParticle(ParticleTypes.SMOKE, var0, var1 + 1.1, var2, 0.0, 0.0, 0.0);
        }
    }
}
