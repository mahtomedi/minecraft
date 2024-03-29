package net.minecraft.util;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
    public static void spawnParticlesOnBlockFaces(Level param0, BlockPos param1, ParticleOptions param2, IntProvider param3) {
        for(Direction var0 : Direction.values()) {
            spawnParticlesOnBlockFace(param0, param1, param2, param3, var0, () -> getRandomSpeedRanges(param0.random), 0.55);
        }

    }

    public static void spawnParticlesOnBlockFace(
        Level param0, BlockPos param1, ParticleOptions param2, IntProvider param3, Direction param4, Supplier<Vec3> param5, double param6
    ) {
        int var0 = param3.sample(param0.random);

        for(int var1 = 0; var1 < var0; ++var1) {
            spawnParticleOnFace(param0, param1, param4, param2, param5.get(), param6);
        }

    }

    private static Vec3 getRandomSpeedRanges(RandomSource param0) {
        return new Vec3(Mth.nextDouble(param0, -0.5, 0.5), Mth.nextDouble(param0, -0.5, 0.5), Mth.nextDouble(param0, -0.5, 0.5));
    }

    public static void spawnParticlesAlongAxis(Direction.Axis param0, Level param1, BlockPos param2, double param3, ParticleOptions param4, UniformInt param5) {
        Vec3 var0 = Vec3.atCenterOf(param2);
        boolean var1 = param0 == Direction.Axis.X;
        boolean var2 = param0 == Direction.Axis.Y;
        boolean var3 = param0 == Direction.Axis.Z;
        int var4 = param5.sample(param1.random);

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

    public static void spawnParticleOnFace(Level param0, BlockPos param1, Direction param2, ParticleOptions param3, Vec3 param4, double param5) {
        Vec3 var0 = Vec3.atCenterOf(param1);
        int var1 = param2.getStepX();
        int var2 = param2.getStepY();
        int var3 = param2.getStepZ();
        double var4 = var0.x + (var1 == 0 ? Mth.nextDouble(param0.random, -0.5, 0.5) : (double)var1 * param5);
        double var5 = var0.y + (var2 == 0 ? Mth.nextDouble(param0.random, -0.5, 0.5) : (double)var2 * param5);
        double var6 = var0.z + (var3 == 0 ? Mth.nextDouble(param0.random, -0.5, 0.5) : (double)var3 * param5);
        double var7 = var1 == 0 ? param4.x() : 0.0;
        double var8 = var2 == 0 ? param4.y() : 0.0;
        double var9 = var3 == 0 ? param4.z() : 0.0;
        param0.addParticle(param3, var4, var5, var6, var7, var8, var9);
    }

    public static void spawnParticleBelow(Level param0, BlockPos param1, RandomSource param2, ParticleOptions param3) {
        double var0 = (double)param1.getX() + param2.nextDouble();
        double var1 = (double)param1.getY() - 0.05;
        double var2 = (double)param1.getZ() + param2.nextDouble();
        param0.addParticle(param3, var0, var1, var2, 0.0, 0.0, 0.0);
    }
}
