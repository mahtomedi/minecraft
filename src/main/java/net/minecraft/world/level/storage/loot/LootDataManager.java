package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataManager implements PreparableReloadListener, LootDataResolver {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataId<LootTable> EMPTY_LOOT_TABLE_KEY = new LootDataId<>(LootDataType.TABLE, BuiltInLootTables.EMPTY);
    private Map<LootDataId<?>, ?> elements = Map.of();
    private Multimap<LootDataType<?>, ResourceLocation> typeKeys = ImmutableMultimap.of();

    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        Map<LootDataType<?>, Map<ResourceLocation, ?>> var0 = new HashMap<>();
        CompletableFuture<?>[] var1 = LootDataType.values()
            .map(param3x -> scheduleElementParse(param3x, param1, param4, var0))
            .toArray(param0x -> new CompletableFuture[param0x]);
        return CompletableFuture.allOf(var1).thenCompose(param0::wait).thenAcceptAsync(param1x -> this.apply(var0), param5);
    }

    private static <T> CompletableFuture<?> scheduleElementParse(
        LootDataType<T> param0, ResourceManager param1, Executor param2, Map<LootDataType<?>, Map<ResourceLocation, ?>> param3
    ) {
        Map<ResourceLocation, T> var0 = new HashMap<>();
        param3.put(param0, var0);
        return CompletableFuture.runAsync(() -> {
            Map<ResourceLocation, JsonElement> var0x = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(param1, param0.directory(), param0.parser(), var0x);
            var0x.forEach((param2x, param3x) -> param0.deserialize(param2x, param3x).ifPresent(param2xx -> var0.put(param2x, param2xx)));
        }, param2);
    }

    private void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> param0) {
        Object var0 = param0.get(LootDataType.TABLE).remove(BuiltInLootTables.EMPTY);
        if (var0 != null) {
            LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", BuiltInLootTables.EMPTY);
        }

        Builder<LootDataId<?>, Object> var1 = ImmutableMap.builder();
        com.google.common.collect.ImmutableMultimap.Builder<LootDataType<?>, ResourceLocation> var2 = ImmutableMultimap.builder();
        param0.forEach((param2, param3) -> param3.forEach((param3x, param4) -> {
                var1.put(new LootDataId(param2, param3x), param4);
                var2.put(param2, param3x);
            }));
        var1.put(EMPTY_LOOT_TABLE_KEY, LootTable.EMPTY);
        final Map<LootDataId<?>, ?> var3 = var1.build();
        ValidationContext var4 = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
            @Nullable
            @Override
            public <T> T getElement(LootDataId<T> param0) {
                return (T)var3.get(param0);
            }
        });
        var3.forEach((param1, param2) -> castAndValidate(var4, param1, param2));
        var4.getProblems().forEach((param0x, param1) -> LOGGER.warn("Found loot table element validation problem in {}: {}", param0x, param1));
        this.elements = var3;
        this.typeKeys = var2.build();
    }

    private static <T> void castAndValidate(ValidationContext param0, LootDataId<T> param1, Object param2) {
        param1.type().runValidation(param0, param1, (T)param2);
    }

    @Nullable
    @Override
    public <T> T getElement(LootDataId<T> param0) {
        return (T)this.elements.get(param0);
    }

    public Collection<ResourceLocation> getKeys(LootDataType<?> param0) {
        return this.typeKeys.get(param0);
    }

    public static LootItemCondition createComposite(LootItemCondition[] param0) {
        return new LootDataManager.CompositePredicate(param0);
    }

    public static LootItemFunction createComposite(LootItemFunction[] param0) {
        return new LootDataManager.FunctionSequence(param0);
    }

    static class CompositePredicate implements LootItemCondition {
        private final LootItemCondition[] terms;
        private final Predicate<LootContext> composedPredicate;

        CompositePredicate(LootItemCondition[] param0) {
            this.terms = param0;
            this.composedPredicate = LootItemConditions.andConditions(param0);
        }

        public final boolean test(LootContext param0) {
            return this.composedPredicate.test(param0);
        }

        @Override
        public void validate(ValidationContext param0) {
            LootItemCondition.super.validate(param0);

            for(int var0 = 0; var0 < this.terms.length; ++var0) {
                this.terms[var0].validate(param0.forChild(".term[" + var0 + "]"));
            }

        }

        @Override
        public LootItemConditionType getType() {
            throw new UnsupportedOperationException();
        }
    }

    static class FunctionSequence implements LootItemFunction {
        protected final LootItemFunction[] functions;
        private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

        public FunctionSequence(LootItemFunction[] param0) {
            this.functions = param0;
            this.compositeFunction = LootItemFunctions.compose(param0);
        }

        public ItemStack apply(ItemStack param0, LootContext param1) {
            return this.compositeFunction.apply(param0, param1);
        }

        @Override
        public void validate(ValidationContext param0) {
            LootItemFunction.super.validate(param0);

            for(int var0 = 0; var0 < this.functions.length; ++var0) {
                this.functions[var0].validate(param0.forChild(".function[" + var0 + "]"));
            }

        }

        @Override
        public LootItemFunctionType getType() {
            throw new UnsupportedOperationException();
        }
    }
}
