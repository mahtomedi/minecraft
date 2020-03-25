package com.mojang.math;

import java.util.Arrays;
import net.minecraft.Util;

public enum SymmetricGroup3 {
    P123(0, 1, 2),
    P213(1, 0, 2),
    P132(0, 2, 1),
    P231(1, 2, 0),
    P312(2, 0, 1),
    P321(2, 1, 0);

    private final int[] permutation;
    private final Matrix3f transformation;
    private static final SymmetricGroup3[][] cayleyTable = Util.make(new SymmetricGroup3[values().length][values().length], param0 -> {
        for(SymmetricGroup3 var0 : values()) {
            for(SymmetricGroup3 var1 : values()) {
                int[] var2 = new int[3];

                for(int var3 = 0; var3 < 3; ++var3) {
                    var2[var3] = var0.permutation[var1.permutation[var3]];
                }

                SymmetricGroup3 var4 = Arrays.stream(values()).filter(param1 -> Arrays.equals(param1.permutation, var2)).findFirst().get();
                param0[var0.ordinal()][var1.ordinal()] = var4;
            }
        }

    });

    private SymmetricGroup3(int param0, int param1, int param2) {
        this.permutation = new int[]{param0, param1, param2};
        this.transformation = new Matrix3f();
        this.transformation.set(0, this.permutation(0), 1.0F);
        this.transformation.set(1, this.permutation(1), 1.0F);
        this.transformation.set(2, this.permutation(2), 1.0F);
    }

    public SymmetricGroup3 compose(SymmetricGroup3 param0) {
        return cayleyTable[this.ordinal()][param0.ordinal()];
    }

    public int permutation(int param0) {
        return this.permutation[param0];
    }

    public Matrix3f transformation() {
        return this.transformation;
    }
}
