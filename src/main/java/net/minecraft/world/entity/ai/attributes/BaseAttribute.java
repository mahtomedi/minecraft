package net.minecraft.world.entity.ai.attributes;

import javax.annotation.Nullable;

public abstract class BaseAttribute implements Attribute {
    private final Attribute parent;
    private final String name;
    private final double defaultValue;
    private boolean syncable;

    protected BaseAttribute(@Nullable Attribute param0, String param1, double param2) {
        this.parent = param0;
        this.name = param1;
        this.defaultValue = param2;
        if (param1 == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public boolean isClientSyncable() {
        return this.syncable;
    }

    public BaseAttribute setSyncable(boolean param0) {
        this.syncable = param0;
        return this;
    }

    @Nullable
    @Override
    public Attribute getParentAttribute() {
        return this.parent;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object param0) {
        return param0 instanceof Attribute && this.name.equals(((Attribute)param0).getName());
    }
}
