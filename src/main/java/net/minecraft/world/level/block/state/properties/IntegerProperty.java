package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class IntegerProperty extends Property<Integer> {
    private final ImmutableSet<Integer> values;

    protected IntegerProperty(String param0, int param1, int param2) {
        super(param0, Integer.class);
        if (param1 < 0) {
            throw new IllegalArgumentException("Min value of " + param0 + " must be 0 or greater");
        } else if (param2 <= param1) {
            throw new IllegalArgumentException("Max value of " + param0 + " must be greater than min (" + param1 + ")");
        } else {
            Set<Integer> var0 = Sets.newHashSet();

            for(int var1 = param1; var1 <= param2; ++var1) {
                var0.add(var1);
            }

            this.values = ImmutableSet.copyOf(var0);
        }
    }

    @Override
    public Collection<Integer> getPossibleValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 instanceof IntegerProperty && super.equals(param0)) {
            IntegerProperty var0 = (IntegerProperty)param0;
            return this.values.equals(var0.values);
        } else {
            return false;
        }
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public static IntegerProperty create(String param0, int param1, int param2) {
        return new IntegerProperty(param0, param1, param2);
    }

    @Override
    public Optional<Integer> getValue(String param0) {
        try {
            Integer var0 = Integer.valueOf(param0);
            return this.values.contains(var0) ? Optional.of(var0) : Optional.empty();
        } catch (NumberFormatException var3) {
            return Optional.empty();
        }
    }

    public String getName(Integer param0) {
        return param0.toString();
    }
}
