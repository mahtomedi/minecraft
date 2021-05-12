package net.minecraft.world.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttributeModifier {
    private static final Logger LOGGER = LogManager.getLogger();
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final Supplier<String> nameGetter;
    private final UUID id;

    public AttributeModifier(String param0, double param1, AttributeModifier.Operation param2) {
        this(Mth.createInsecureUUID(ThreadLocalRandom.current()), () -> param0, param1, param2);
    }

    public AttributeModifier(UUID param0, String param1, double param2, AttributeModifier.Operation param3) {
        this(param0, () -> param1, param2, param3);
    }

    public AttributeModifier(UUID param0, Supplier<String> param1, double param2, AttributeModifier.Operation param3) {
        this.id = param0;
        this.nameGetter = param1;
        this.amount = param2;
        this.operation = param3;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.nameGetter.get();
    }

    public AttributeModifier.Operation getOperation() {
        return this.operation;
    }

    public double getAmount() {
        return this.amount;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            AttributeModifier var0 = (AttributeModifier)param0;
            return Objects.equals(this.id, var0.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "AttributeModifier{amount="
            + this.amount
            + ", operation="
            + this.operation
            + ", name='"
            + (String)this.nameGetter.get()
            + "', id="
            + this.id
            + "}";
    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", this.getName());
        var0.putDouble("Amount", this.amount);
        var0.putInt("Operation", this.operation.toValue());
        var0.putUUID("UUID", this.id);
        return var0;
    }

    @Nullable
    public static AttributeModifier load(CompoundTag param0) {
        try {
            UUID var0 = param0.getUUID("UUID");
            AttributeModifier.Operation var1 = AttributeModifier.Operation.fromValue(param0.getInt("Operation"));
            return new AttributeModifier(var0, param0.getString("Name"), param0.getDouble("Amount"), var1);
        } catch (Exception var3) {
            LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
            return null;
        }
    }

    public static enum Operation {
        ADDITION(0),
        MULTIPLY_BASE(1),
        MULTIPLY_TOTAL(2);

        private static final AttributeModifier.Operation[] OPERATIONS = new AttributeModifier.Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
        private final int value;

        private Operation(int param0) {
            this.value = param0;
        }

        public int toValue() {
            return this.value;
        }

        public static AttributeModifier.Operation fromValue(int param0) {
            if (param0 >= 0 && param0 < OPERATIONS.length) {
                return OPERATIONS[param0];
            } else {
                throw new IllegalArgumentException("No operation with value " + param0);
            }
        }
    }
}
