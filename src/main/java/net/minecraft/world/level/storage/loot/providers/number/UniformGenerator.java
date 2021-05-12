package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class UniformGenerator implements NumberProvider {
    final NumberProvider min;
    final NumberProvider max;

    UniformGenerator(NumberProvider param0, NumberProvider param1) {
        this.min = param0;
        this.max = param1;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.UNIFORM;
    }

    public static UniformGenerator between(float param0, float param1) {
        return new UniformGenerator(ConstantValue.exactly(param0), ConstantValue.exactly(param1));
    }

    @Override
    public int getInt(LootContext param0) {
        return Mth.nextInt(param0.getRandom(), this.min.getInt(param0), this.max.getInt(param0));
    }

    @Override
    public float getFloat(LootContext param0) {
        return Mth.nextFloat(param0.getRandom(), this.min.getFloat(param0), this.max.getFloat(param0));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<UniformGenerator> {
        public UniformGenerator deserialize(JsonObject param0, JsonDeserializationContext param1) {
            NumberProvider var0 = GsonHelper.getAsObject(param0, "min", param1, NumberProvider.class);
            NumberProvider var1 = GsonHelper.getAsObject(param0, "max", param1, NumberProvider.class);
            return new UniformGenerator(var0, var1);
        }

        public void serialize(JsonObject param0, UniformGenerator param1, JsonSerializationContext param2) {
            param0.add("min", param2.serialize(param1.min));
            param0.add("max", param2.serialize(param1.max));
        }
    }
}
