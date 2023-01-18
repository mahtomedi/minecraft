package net.minecraft.world.entity;

import java.util.EnumSet;
import java.util.Set;

public enum RelativeMovement {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4);

    public static final Set<RelativeMovement> ALL = Set.of(values());
    public static final Set<RelativeMovement> ROTATION = Set.of(X_ROT, Y_ROT);
    private final int bit;

    private RelativeMovement(int param0) {
        this.bit = param0;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int param0) {
        return (param0 & this.getMask()) == this.getMask();
    }

    public static Set<RelativeMovement> unpack(int param0) {
        Set<RelativeMovement> var0 = EnumSet.noneOf(RelativeMovement.class);

        for(RelativeMovement var1 : values()) {
            if (var1.isSet(param0)) {
                var0.add(var1);
            }
        }

        return var0;
    }

    public static int pack(Set<RelativeMovement> param0) {
        int var0 = 0;

        for(RelativeMovement var1 : param0) {
            var0 |= var1.getMask();
        }

        return var0;
    }
}
