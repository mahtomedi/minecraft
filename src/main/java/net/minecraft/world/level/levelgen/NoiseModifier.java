package net.minecraft.world.level.levelgen;

@FunctionalInterface
public interface NoiseModifier {
    NoiseModifier PASSTHROUGH = (param0, param1, param2, param3) -> param0;

    double modifyNoise(double var1, int var3, int var4, int var5);
}
