package net.minecraft;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
    boolean test(char var1);

    default CharPredicate and(CharPredicate param0) {
        Objects.requireNonNull(param0);
        return param1 -> this.test(param1) && param0.test(param1);
    }

    default CharPredicate negate() {
        return param0 -> !this.test(param0);
    }

    default CharPredicate or(CharPredicate param0) {
        Objects.requireNonNull(param0);
        return param1 -> this.test(param1) || param0.test(param1);
    }
}
