package net.minecraft.world.level.levelgen;

import java.util.function.Function;
import net.minecraft.world.level.levelgen.blending.Blender;

public interface DensityFunction {
    double compute(DensityFunction.FunctionContext var1);

    void fillArray(double[] var1, DensityFunction.ContextProvider var2);

    DensityFunction mapAll(DensityFunction.Visitor var1);

    double minValue();

    double maxValue();

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
