package net.minecraft.world.entity.ai.attributes;

public class Attribute {
    private final double defaultValue;
    private boolean syncable;
    private final String descriptionId;

    protected Attribute(String param0, double param1) {
        this.defaultValue = param1;
        this.descriptionId = param0;
    }

    public double getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isClientSyncable() {
        return this.syncable;
    }

    public Attribute setSyncable(boolean param0) {
        this.syncable = param0;
        return this;
    }

    public double sanitizeValue(double param0) {
        return param0;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }
}
