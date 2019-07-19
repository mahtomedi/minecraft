package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MyceliumBlock extends SpreadingSnowyDirtBlock {
    public MyceliumBlock(Block.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        super.animateTick(param0, param1, param2, param3);
        if (param3.nextInt(10) == 0) {
            param1.addParticle(
                ParticleTypes.MYCELIUM,
                (double)((float)param2.getX() + param3.nextFloat()),
                (double)((float)param2.getY() + 1.1F),
                (double)((float)param2.getZ() + param3.nextFloat()),
                0.0,
                0.0,
                0.0
            );
        }

    }
}
