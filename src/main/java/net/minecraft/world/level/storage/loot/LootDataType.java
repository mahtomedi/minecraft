package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataType<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(LootItemConditions.CODEC, "predicates", createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(LootItemFunctions.CODEC, "item_modifiers", createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<>(LootTable.CODEC, "loot_tables", createLootTableValidator());
    private final Codec<T> codec;
    private final String directory;
    private final LootDataType.Validator<T> validator;

    private LootDataType(Codec<T> param0, String param1, LootDataType.Validator<T> param2) {
        this.codec = param0;
        this.directory = param1;
        this.validator = param2;
    }

    public String directory() {
        return this.directory;
    }

    public void runValidation(ValidationContext param0, LootDataId<T> param1, T param2) {
        this.validator.run(param0, param1, param2);
    }

    public Optional<T> deserialize(ResourceLocation param0, JsonElement param1) {
        DataResult<T> var0 = this.codec.parse(JsonOps.INSTANCE, param1);
        var0.error().ifPresent(param1x -> LOGGER.error("Couldn't parse element {}:{} - {}", this.directory, param0, param1x.message()));
        return var0.result();
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(PREDICATE, MODIFIER, TABLE);
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
