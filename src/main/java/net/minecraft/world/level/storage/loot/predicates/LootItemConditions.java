package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class LootItemConditions {
    public static final LootItemConditionType INVERTED = register("inverted", new InvertedLootItemCondition.Serializer());
    public static final LootItemConditionType ALTERNATIVE = register("alternative", new AlternativeLootItemCondition.Serializer());
    public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", new LootItemRandomChanceCondition.Serializer());
    public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register(
        "random_chance_with_looting", new LootItemRandomChanceWithLootingCondition.Serializer()
    );
    public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", new LootItemEntityPropertyCondition.Serializer());
    public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", new LootItemKilledByPlayerCondition.Serializer());
    public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", new EntityHasScoreCondition.Serializer());
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", new LootItemBlockStatePropertyCondition.Serializer());
    public static final LootItemConditionType MATCH_TOOL = register("match_tool", new MatchTool.Serializer());
    public static final LootItemConditionType TABLE_BONUS = register("table_bonus", new BonusLevelTableCondition.Serializer());
    public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", new ExplosionCondition.Serializer());
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", new DamageSourceCondition.Serializer());
    public static final LootItemConditionType LOCATION_CHECK = register("location_check", new LocationCheck.Serializer());
    public static final LootItemConditionType WEATHER_CHECK = register("weather_check", new WeatherCheck.Serializer());
    public static final LootItemConditionType REFERENCE = register("reference", new ConditionReference.Serializer());
    public static final LootItemConditionType TIME_CHECK = register("time_check", new TimeCheck.Serializer());
    public static final LootItemConditionType VALUE_CHECK = register("value_check", new ValueCheckCondition.Serializer());

    private static LootItemConditionType register(String param0, Serializer<? extends LootItemCondition> param1) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(param0), new LootItemConditionType(param1));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_CONDITION_TYPE, "condition", "condition", LootItemCondition::getType).build();
    }

    public static <T> Predicate<T> andConditions(Predicate<T>[] param0) {
        switch(param0.length) {
            case 0:
                return param0x -> true;
            case 1:
                return param0[0];
            case 2:
                return param0[0].and(param0[1]);
            default:
                return param1 -> {
                    for(Predicate<T> var0x : param0) {
                        if (!var0x.test(param1)) {
                            return false;
                        }
                    }

                    return true;
                };
        }
    }

    public static <T> Predicate<T> orConditions(Predicate<T>[] param0) {
        switch(param0.length) {
            case 0:
                return param0x -> false;
            case 1:
                return param0[0];
            case 2:
                return param0[0].or(param0[1]);
            default:
                return param1 -> {
                    for(Predicate<T> var0x : param0) {
                        if (var0x.test(param1)) {
                            return true;
                        }
                    }

                    return false;
                };
        }
    }
}
