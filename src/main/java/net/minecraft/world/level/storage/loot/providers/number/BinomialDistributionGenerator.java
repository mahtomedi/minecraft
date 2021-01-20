package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class BinomialDistributionGenerator implements NumberProvider {
    private final NumberProvider n;
    private final NumberProvider p;

    private BinomialDistributionGenerator(NumberProvider param0, NumberProvider param1) {
        this.n = param0;
        this.p = param1;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.BINOMIAL;
    }

    @Override
    public int getInt(LootContext param0) {
        int var0 = this.n.getInt(param0);
        float var1 = this.p.getFloat(param0);
        Random var2 = param0.getRandom();
        int var3 = 0;

        for(int var4 = 0; var4 < var0; ++var4) {
            if (var2.nextFloat() < var1) {
                ++var3;
            }
        }

        return var3;
    }

    @Override
    public float getFloat(LootContext param0) {
        return (float)this.getInt(param0);
    }

    public static BinomialDistributionGenerator binomial(int param0, float param1) {
        return new BinomialDistributionGenerator(ConstantValue.exactly((float)param0), ConstantValue.exactly(param1));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BinomialDistributionGenerator> {
        public BinomialDistributionGenerator deserialize(JsonObject param0, JsonDeserializationContext param1) {
            NumberProvider var0 = GsonHelper.getAsObject(param0, "n", param1, NumberProvider.class);
            NumberProvider var1 = GsonHelper.getAsObject(param0, "p", param1, NumberProvider.class);
            return new BinomialDistributionGenerator(var0, var1);
        }

        public void serialize(JsonObject param0, BinomialDistributionGenerator param1, JsonSerializationContext param2) {
            param0.add("n", param2.serialize(param1.n));
            param0.add("p", param2.serialize(param1.p));
        }
    }
}
