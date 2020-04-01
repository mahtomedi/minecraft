package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G11 extends NormalDimension {
    public G11(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    private static Vector3f getColor(BlockPos param0) {
        double var0 = (double)param0.getX();
        double var1 = (double)param0.getZ();
        double var2 = Math.sqrt(var0 * var0 + var1 * var1);
        float var3 = (float)Mth.clamp(1.0 - Mth.pct(var2, 50.0, 100.0), 0.0, 1.0);
        return new Vector3f(var3, var3, var3);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
        return getColor(param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
        return getColor(param0.blockPosition());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return Vec3.ZERO;
    }
}
