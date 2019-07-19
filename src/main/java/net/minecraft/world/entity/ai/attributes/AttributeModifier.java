package net.minecraft.world.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.util.Mth;

public class AttributeModifier {
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final Supplier<String> nameGetter;
    private final UUID id;
    private boolean serialize = true;

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

    public boolean isSerializable() {
        return this.serialize;
    }

    public AttributeModifier setSerialize(boolean param0) {
        this.serialize = param0;
        return this;
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
        return this.id != null ? this.id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AttributeModifier{amount="
            + this.amount
            + ", operation="
            + this.operation
            + ", name='"
            + (String)this.nameGetter.get()
            + '\''
            + ", id="
            + this.id
            + ", serialize="
            + this.serialize
            + '}';
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
