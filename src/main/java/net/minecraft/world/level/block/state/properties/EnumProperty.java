package net.minecraft.world.level.block.state.properties;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

public class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {
    private final ImmutableSet<T> values;
    private final Map<String, T> names = Maps.newHashMap();

    protected EnumProperty(String param0, Class<T> param1, Collection<T> param2) {
        super(param0, param1);
        this.values = ImmutableSet.copyOf(param2);

        for(T var0 : param2) {
            String var1 = var0.getSerializedName();
            if (this.names.containsKey(var1)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + var1 + "'");
            }

            this.names.put(var1, var0);
        }

    }

    @Override
    public Collection<T> getPossibleValues() {
        return this.values;
    }

    @Override
    public Optional<T> getValue(String param0) {
        return Optional.ofNullable(this.names.get(param0));
    }

    public String getName(T param0) {
        return param0.getSerializedName();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 instanceof EnumProperty var0 && super.equals(param0)) {
            return this.values.equals(var0.values) && this.names.equals(var0.names);
        } else {
            return false;
        }
    }

    @Override
    public int generateHashCode() {
        int var0 = super.generateHashCode();
        var0 = 31 * var0 + this.values.hashCode();
        return 31 * var0 + this.names.hashCode();
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String param0, Class<T> param1) {
        return create(param0, param1, Predicates.alwaysTrue());
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String param0, Class<T> param1, Predicate<T> param2) {
        return create(param0, param1, Arrays.<T>stream(param1.getEnumConstants()).filter(param2).collect(Collectors.toList()));
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String param0, Class<T> param1, T... param2) {
        return create(param0, param1, Lists.newArrayList(param2));
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String param0, Class<T> param1, Collection<T> param2) {
        return new EnumProperty<>(param0, param1, param2);
    }
}
