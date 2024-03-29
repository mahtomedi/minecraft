package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public class AttributeModifier {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<AttributeModifier> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    UUIDUtil.CODEC.fieldOf("UUID").forGetter(AttributeModifier::getId),
                    Codec.STRING.fieldOf("Name").forGetter(param0x -> param0x.name),
                    Codec.DOUBLE.fieldOf("Amount").forGetter(AttributeModifier::getAmount),
                    AttributeModifier.Operation.CODEC.fieldOf("Operation").forGetter(AttributeModifier::getOperation)
                )
                .apply(param0, AttributeModifier::new)
    );
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final String name;
    private final UUID id;

    public AttributeModifier(String param0, double param1, AttributeModifier.Operation param2) {
        this(Mth.createInsecureUUID(RandomSource.createNewThreadLocalInstance()), param0, param1, param2);
    }

    public AttributeModifier(UUID param0, String param1, double param2, AttributeModifier.Operation param3) {
        this.id = param0;
        this.name = param1;
        this.amount = param2;
        this.operation = param3;
    }

    public UUID getId() {
        return this.id;
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
        return "AttributeModifier{amount=" + this.amount + ", operation=" + this.operation + ", name='" + this.name + "', id=" + this.id + "}";
    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", this.name);
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

    public static enum Operation implements StringRepresentable {
        ADDITION("addition", 0),
        MULTIPLY_BASE("multiply_base", 1),
        MULTIPLY_TOTAL("multiply_total", 2);

        private static final AttributeModifier.Operation[] OPERATIONS = new AttributeModifier.Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
        public static final Codec<AttributeModifier.Operation> CODEC = StringRepresentable.fromEnum(AttributeModifier.Operation::values);
        private final String name;
        private final int value;

        private Operation(String param0, int param1) {
            this.name = param0;
            this.value = param1;
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

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
