package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ExplosionCondition implements LootItemCondition {
    private static final ExplosionCondition INSTANCE = new ExplosionCondition();
    public static final Codec<ExplosionCondition> CODEC = Codec.unit(INSTANCE);

    private ExplosionCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.SURVIVES_EXPLOSION;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
    }

    public boolean test(LootContext param0) {
        Float var0 = param0.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (var0 != null) {
            RandomSource var1 = param0.getRandom();
            float var2 = 1.0F / var0;
            return var1.nextFloat() <= var2;
        } else {
            return true;
        }
    }

    public static LootItemCondition.Builder survivesExplosion() {
        return () -> INSTANCE;
    }
}
