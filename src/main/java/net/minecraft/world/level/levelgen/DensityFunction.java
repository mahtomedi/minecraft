package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.blending.Blender;

public interface DensityFunction {
    Codec<DensityFunction> DIRECT_CODEC = DensityFunctions.DIRECT_CODEC;
    Codec<Holder<DensityFunction>> CODEC = RegistryFileCodec.create(Registry.DENSITY_FUNCTION_REGISTRY, DIRECT_CODEC);
    Codec<DensityFunction> HOLDER_HELPER_CODEC = CODEC.xmap(
        DensityFunctions.HolderHolder::new,
        param0 -> (Holder<DensityFunction>)(param0 instanceof DensityFunctions.HolderHolder var0 ? var0.function() : new Holder.Direct<>(param0))
    );

    double compute(DensityFunction.FunctionContext var1);

    void fillArray(double[] var1, DensityFunction.ContextProvider var2);

    DensityFunction mapAll(DensityFunction.Visitor var1);

    double minValue();

    double maxValue();

    Codec<? extends DensityFunction> codec();

    default DensityFunction clamp(double param0, double param1) {
        return new DensityFunctions.Clamp(this, param0, param1);
    }

    default DensityFunction abs() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
    }

    default DensityFunction square() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
    }

    default DensityFunction cube() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
    }

    default DensityFunction halfNegative() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
    }

    default DensityFunction quarterNegative() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
    }

    default DensityFunction squeeze() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
    }

    public interface ContextProvider {
        DensityFunction.FunctionContext forIndex(int var1);

        void fillAllDirectly(double[] var1, DensityFunction var2);
    }

    public interface FunctionContext {
        int blockX();

        int blockY();

        int blockZ();

        default Blender getBlender() {
            return Blender.empty();
        }
    }

    public interface SimpleFunction extends DensityFunction {
        @Override
        default void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        default DensityFunction mapAll(DensityFunction.Visitor param0) {
            return param0.apply(this);
        }
    }

    public static record SinglePointContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
    }

    public interface Visitor extends Function<DensityFunction, DensityFunction> {
    }
}
