package net.minecraft.network.protocol.game;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.VisibleForTesting;

public class VecDeltaCodec {
    private static final double TRUNCATION_STEPS = 4096.0;
    private Vec3 base = Vec3.ZERO;

    @VisibleForTesting
    static long encode(double param0) {
        return Math.round(param0 * 4096.0);
    }

    @VisibleForTesting
    static double decode(long param0) {
        return (double)param0 / 4096.0;
    }

    public Vec3 decode(long param0, long param1, long param2) {
        if (param0 == 0L && param1 == 0L && param2 == 0L) {
            return this.base;
        } else {
            double var0 = param0 == 0L ? this.base.x : decode(encode(this.base.x) + param0);
            double var1 = param1 == 0L ? this.base.y : decode(encode(this.base.y) + param1);
            double var2 = param2 == 0L ? this.base.z : decode(encode(this.base.z) + param2);
            return new Vec3(var0, var1, var2);
        }
    }

    public long encodeX(Vec3 param0) {
        return encode(param0.x) - encode(this.base.x);
    }

    public long encodeY(Vec3 param0) {
        return encode(param0.y) - encode(this.base.y);
    }

    public long encodeZ(Vec3 param0) {
        return encode(param0.z) - encode(this.base.z);
    }

    public Vec3 delta(Vec3 param0) {
        return param0.subtract(this.base);
    }

    public void setBase(Vec3 param0) {
        this.base = param0;
    }
}
