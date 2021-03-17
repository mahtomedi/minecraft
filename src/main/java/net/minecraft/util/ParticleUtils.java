package net.minecraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleUtils {
    public static void spawnParticlesOnBlockFaces(Level param0, BlockPos param1, ParticleOptions param2, IntRange param3) {
        for(Direction var0 : Direction.values()) {
            int var1 = param3.randomValue(param0.random);

            for(int var2 = 0; var2 < var1; ++var2) {
                spawnParticleOnFace(param0, param1, var0, param2);
            }
        }

    }

    public static void spawnParticlesAlongAxis(Direction.Axis param0, Level param1, BlockPos param2, double param3, ParticleOptions param4, IntRange param5) {
        Vec3 var0 = Vec3.atCenterOf(param2);
        boolean var1 = param0 == Direction.Axis.X;
        boolean var2 = param0 == Direction.Axis.Y;
        boolean var3 = param0 == Direction.Axis.Z;
        int var4 = param5.randomValue(param1.random);

        for(int var5 = 0; var5 < var4; ++var5) {
            double var6 = var0.x + Mth.nextDouble(param1.random, -1.0, 1.0) * (var1 ? 0.5 : param3);
            double var7 = var0.y + Mth.nextDouble(param1.random, -1.0, 1.0) * (var2 ? 0.5 : param3);
            double var8 = var0.z + Mth.nextDouble(param1.random, -1.0, 1.0) * (var3 ? 0.5 : param3);
            double var9 = var1 ? Mth.nextDouble(param1.random, -1.0, 1.0) : 0.0;
            double var10 = var2 ? Mth.nextDouble(param1.random, -1.0, 1.0) : 0.0;
            double var11 = var3 ? Mth.nextDouble(param1.random, -1.0, 1.0) : 0.0;
            param1.addParticle(param4, var6, var7, var8, var9, var10, var11);
        }

    }

    public static void spawnParticleOnFace(Level param0, BlockPos param1, Direction param2, ParticleOptions param3) {
        Vec3 var0 = Vec3.atCenterOf(param1);
        int var1 = param2.getStepX();
        int var2 = param2.getStepY();
        int var3 = param2.getStepZ();
        double var4 = var0.x + (var1 == 0 ? Mth.nextDouble(param0.random, -0.5, 0.5) : (double)var1 * 0.55);
        double var5 = var0.y + (var2 == 0 ? Mth.nextDouble(param0.random, -0.5, 0.5) : (double)var2 * 0.55);
        double var6 = var0.z + (var3 == 0 ? Mth.nextDouble(param0.random, -0.5, 0.5) : (double)var3 * 0.55);
        double var7 = var1 == 0 ? Mth.nextDouble(param0.random, -1.0, 1.0) : 0.0;
        double var8 = var2 == 0 ? Mth.nextDouble(param0.random, -1.0, 1.0) : 0.0;
        double var9 = var3 == 0 ? Mth.nextDouble(param0.random, -1.0, 1.0) : 0.0;
        param0.addParticle(param3, var4, var5, var6, var7, var8, var9);
    }
}
