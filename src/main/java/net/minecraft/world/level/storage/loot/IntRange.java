package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class IntRange {
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;
    private final IntRange.IntLimiter limiter;
    private final IntRange.IntChecker predicate;

    public Set<LootContextParam<?>> getReferencedContextParams() {
        Builder<LootContextParam<?>> var0 = ImmutableSet.builder();
        if (this.min != null) {
            var0.addAll(this.min.getReferencedContextParams());
        }

        if (this.max != null) {
            var0.addAll(this.max.getReferencedContextParams());
        }

        return var0.build();
    }

    private IntRange(@Nullable NumberProvider param0, @Nullable NumberProvider param1) {
        this.min = param0;
        this.max = param1;
        if (param0 == null) {
            if (param1 == null) {
                this.limiter = (param0x, param1x) -> param1x;
                this.predicate = (param0x, param1x) -> true;
            } else {
                this.limiter = (param1x, param2) -> Math.min(param1.getInt(param1x), param2);
                this.predicate = (param1x, param2) -> param2 <= param1.getInt(param1x);
            }
        } else if (param1 == null) {
            this.limiter = (param1x, param2) -> Math.max(param0.getInt(param1x), param2);
            this.predicate = (param1x, param2) -> param2 >= param0.getInt(param1x);
        } else {
            this.limiter = (param2, param3) -> Mth.clamp(param3, param0.getInt(param2), param1.getInt(param2));
            this.predicate = (param2, param3) -> param3 >= param0.getInt(param2) && param3 <= param1.getInt(param2);
        }

    }

    public static IntRange exact(int param0) {
        ConstantValue var0 = ConstantValue.exactly((float)param0);
        return new IntRange(var0, var0);
    }

    public static IntRange range(int param0, int param1) {
        return new IntRange(ConstantValue.exactly((float)param0), ConstantValue.exactly((float)param1));
    }

    public static IntRange lowerBound(int param0) {
        return new IntRange(ConstantValue.exactly((float)param0), null);
    }

    public static IntRange upperBound(int param0) {
        return new IntRange(null, ConstantValue.exactly((float)param0));
    }

    public int clamp(LootContext param0, int param1) {
        return this.limiter.apply(param0, param1);
    }

    public boolean test(LootContext param0, int param1) {
        return this.predicate.test(param0, param1);
    }

    @FunctionalInterface
    interface IntChecker {
        boolean test(LootContext var1, int var2);
    }

    @FunctionalInterface
    interface IntLimiter {
        int apply(LootContext var1, int var2);
    }

    public static class Serializer implements JsonDeserializer<IntRange>, JsonSerializer<IntRange> {
        public IntRange deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) {
            if (param0.isJsonPrimitive()) {
                return IntRange.exact(param0.getAsInt());
            } else {
                JsonObject var0 = GsonHelper.convertToJsonObject(param0, "value");
                NumberProvider var1 = var0.has("min") ? GsonHelper.getAsObject(var0, "min", param2, NumberProvider.class) : null;
                NumberProvider var2 = var0.has("max") ? GsonHelper.getAsObject(var0, "max", param2, NumberProvider.class) : null;
                return new IntRange(var1, var2);
            }
        }

        public JsonElement serialize(IntRange param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            if (Objects.equals(param0.max, param0.min)) {
                return param2.serialize(param0.min);
            } else {
                if (param0.max != null) {
                    var0.add("max", param2.serialize(param0.max));
                }

                if (param0.min != null) {
                    var0.add("min", param2.serialize(param0.min));
                }

                return var0;
            }
        }
    }
}
