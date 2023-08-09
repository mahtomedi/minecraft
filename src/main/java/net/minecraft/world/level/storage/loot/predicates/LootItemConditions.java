package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class LootItemConditions {
    private static final Codec<LootItemCondition> TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE
        .byNameCodec()
        .dispatch("condition", LootItemCondition::getType, LootItemConditionType::codec);
    public static final Codec<LootItemCondition> CODEC = ExtraCodecs.lazyInitializedCodec(
        () -> ExtraCodecs.withAlternative(TYPED_CODEC, AllOfCondition.INLINE_CODEC)
    );
    public static final LootItemConditionType INVERTED = register("inverted", InvertedLootItemCondition.CODEC);
    public static final LootItemConditionType ANY_OF = register("any_of", AnyOfCondition.CODEC);
    public static final LootItemConditionType ALL_OF = register("all_of", AllOfCondition.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", LootItemRandomChanceCondition.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register(
        "random_chance_with_looting", LootItemRandomChanceWithLootingCondition.CODEC
    );
    public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", LootItemEntityPropertyCondition.CODEC);
    public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", LootItemKilledByPlayerCondition.CODEC);
    public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", EntityHasScoreCondition.CODEC);
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", LootItemBlockStatePropertyCondition.CODEC);
    public static final LootItemConditionType MATCH_TOOL = register("match_tool", MatchTool.CODEC);
    public static final LootItemConditionType TABLE_BONUS = register("table_bonus", BonusLevelTableCondition.CODEC);
    public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", ExplosionCondition.CODEC);
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", DamageSourceCondition.CODEC);
    public static final LootItemConditionType LOCATION_CHECK = register("location_check", LocationCheck.CODEC);
    public static final LootItemConditionType WEATHER_CHECK = register("weather_check", WeatherCheck.CODEC);
    public static final LootItemConditionType REFERENCE = register("reference", ConditionReference.CODEC);
    public static final LootItemConditionType TIME_CHECK = register("time_check", TimeCheck.CODEC);
    public static final LootItemConditionType VALUE_CHECK = register("value_check", ValueCheckCondition.CODEC);

    private static LootItemConditionType register(String param0, Codec<? extends LootItemCondition> param1) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(param0), new LootItemConditionType(param1));
    }

    public static <T> Predicate<T> andConditions(List<? extends Predicate<T>> param0) {
        List<Predicate<T>> var0 = List.copyOf(param0);

        return switch(var0.size()) {
            case 0 -> param0x -> true;
            case 1 -> (Predicate)var0.get(0);
            case 2 -> var0.get(0).and(var0.get(1));
            default -> param1 -> {
            for(Predicate<T> var0x : var0) {
                if (!var0x.test((T)param1)) {
                    return false;
                }
            }

            return true;
        };
        };
    }

    public static <T> Predicate<T> orConditions(List<? extends Predicate<T>> param0) {
        List<Predicate<T>> var0 = List.copyOf(param0);

        return switch(var0.size()) {
            case 0 -> param0x -> false;
            case 1 -> (Predicate)var0.get(0);
            case 2 -> var0.get(0).or(var0.get(1));
            default -> param1 -> {
            for(Predicate<T> var0x : var0) {
                if (var0x.test((T)param1)) {
                    return true;
                }
            }

            return false;
        };
        };
    }
}
