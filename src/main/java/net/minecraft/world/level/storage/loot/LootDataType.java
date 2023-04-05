package net.minecraft.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootDataType<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(
        Deserializers.createConditionSerializer().create(),
        createSingleOrMultipleDeserialiser(LootItemCondition.class, LootDataManager::createComposite),
        "predicates",
        createSimpleValidator()
    );
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(
        Deserializers.createFunctionSerializer().create(),
        createSingleOrMultipleDeserialiser(LootItemFunction.class, LootDataManager::createComposite),
        "item_modifiers",
        createSimpleValidator()
    );
    public static final LootDataType<LootTable> TABLE = new LootDataType<>(
        Deserializers.createLootTableSerializer().create(), createSingleDeserialiser(LootTable.class), "loot_tables", createLootTableValidator()
    );
    private final Gson parser;
    private final BiFunction<ResourceLocation, JsonElement, Optional<T>> topDeserializer;
    private final String directory;
    private final LootDataType.Validator<T> validator;

    private LootDataType(
        Gson param0, BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> param1, String param2, LootDataType.Validator<T> param3
    ) {
        this.parser = param0;
        this.directory = param2;
        this.validator = param3;
        this.topDeserializer = param1.apply(param0, param2);
    }

    public Gson parser() {
        return this.parser;
    }

    public String directory() {
        return this.directory;
    }

    public void runValidation(ValidationContext param0, LootDataId<T> param1, T param2) {
        this.validator.run(param0, param1, param2);
    }

    public Optional<T> deserialize(ResourceLocation param0, JsonElement param1) {
        return this.topDeserializer.apply(param0, param1);
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(PREDICATE, MODIFIER, TABLE);
    }

    private static <T> BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> createSingleDeserialiser(Class<T> param0) {
        return (param1, param2) -> (param3, param4) -> {
                try {
                    return Optional.of(param1.fromJson(param4, param0));
                } catch (Exception var6) {
                    LOGGER.error("Couldn't parse element {}:{}", param2, param3, var6);
                    return Optional.empty();
                }
            };
    }

    private static <T> BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> createSingleOrMultipleDeserialiser(
        Class<T> param0, Function<T[], T> param1
    ) {
        Class<T[]> var0 = param0.arrayType();
        return (param3, param4) -> (param5, param6) -> {
                try {
                    if (param6.isJsonArray()) {
                        T[] var1x = param3.fromJson(param6, var0);
                        return Optional.of(param1.apply(var1x));
                    } else {
                        return Optional.of(param3.fromJson(param6, param0));
                    }
                } catch (Exception var8) {
                    LOGGER.error("Couldn't parse element {}:{}", param4, param5, var8);
                    return Optional.empty();
                }
            };
    }

    private static <T extends LootContextUser> LootDataType.Validator<T> createSimpleValidator() {
        return (param0, param1, param2) -> param2.validate(param0.enterElement("{" + param1.type().directory + ":" + param1.location() + "}", param1));
    }

    private static LootDataType.Validator<LootTable> createLootTableValidator() {
        return (param0, param1, param2) -> param2.validate(
                param0.setParams(param2.getParamSet()).enterElement("{" + param1.type().directory + ":" + param1.location() + "}", param1)
            );
    }

    @FunctionalInterface
    public interface Validator<T> {
        void run(ValidationContext var1, LootDataId<T> var2, T var3);
    }
}
