package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;

public abstract class AbstractProperty<T extends Comparable<T>> implements Property<T> {
    private final Class<T> clazz;
    private final String name;
    private Integer hashCode;

    protected AbstractProperty(String param0, Class<T> param1) {
        this.clazz = param1;
        this.name = param0;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<T> getValueClass() {
        return this.clazz;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof AbstractProperty)) {
            return false;
        } else {
            AbstractProperty<?> var0 = (AbstractProperty)param0;
            return this.clazz.equals(var0.clazz) && this.name.equals(var0.name);
        }
    }

    @Override
    public final int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.generateHashCode();
        }

        return this.hashCode;
    }

    public int generateHashCode() {
        return 31 * this.clazz.hashCode() + this.name.hashCode();
    }
}
