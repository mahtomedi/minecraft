package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;

public class BooleanProperty extends Property<Boolean> {
    private final ImmutableSet<Boolean> values = ImmutableSet.of(true, false);

    protected BooleanProperty(String param0) {
        super(param0, Boolean.class);
    }

    @Override
    public Collection<Boolean> getPossibleValues() {
        return this.values;
    }

    public static BooleanProperty create(String param0) {
        return new BooleanProperty(param0);
    }

    @Override
    public Optional<Boolean> getValue(String param0) {
        return !"true".equals(param0) && !"false".equals(param0) ? Optional.empty() : Optional.of(Boolean.valueOf(param0));
    }

    public String getName(Boolean param0) {
        return param0.toString();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof BooleanProperty var0 && super.equals(param0)) {
                return this.values.equals(var0.values);
            }

            return false;
        }
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }
}
