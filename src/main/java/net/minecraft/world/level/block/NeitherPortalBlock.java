package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.NeitherPortalEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NeitherPortalBlock extends PortalBlock implements EntityBlock {
    private static final Random I_DONT_CARE_ABOUT_THREADS = new Random();

    public NeitherPortalBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected ParticleOptions getParticleType(BlockState param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof NeitherPortalEntity) {
            int var1 = ((NeitherPortalEntity)var0).getDimension();
            Vec3 var2 = Vec3.fromRGB24(var1);
            double var3 = 1.0 + (double)(var1 >> 16 & 0xFF) / 255.0;
            return new DustParticleOptions((float)var2.x, (float)var2.y, (float)var2.z, (float)var3);
        } else {
            return super.getParticleType(param0, param1, param2);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new NeitherPortalEntity(Math.abs(I_DONT_CARE_ABOUT_THREADS.nextInt()));
    }
}
