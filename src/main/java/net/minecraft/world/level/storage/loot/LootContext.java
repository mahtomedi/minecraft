package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
    private final RandomSource random;
    private final float luck;
    private final ServerLevel level;
    private final LootDataResolver lootDataResolver;
    private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.newLinkedHashSet();
    private final Map<LootContextParam<?>, Object> params;
    private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops;

    LootContext(
        RandomSource param0,
        float param1,
        ServerLevel param2,
        LootDataResolver param3,
        Map<LootContextParam<?>, Object> param4,
        Map<ResourceLocation, LootContext.DynamicDrop> param5
    ) {
        this.random = param0;
        this.luck = param1;
        this.level = param2;
        this.lootDataResolver = param3;
        this.params = ImmutableMap.copyOf(param4);
        this.dynamicDrops = ImmutableMap.copyOf(param5);
    }

    public boolean hasParam(LootContextParam<?> param0) {
        return this.params.containsKey(param0);
    }

    public <T> T getParam(LootContextParam<T> param0) {
        T var0 = (T)this.params.get(param0);
        if (var0 == null) {
            throw new NoSuchElementException(param0.getName().toString());
        } else {
            return var0;
        }
    }

    public void addDynamicDrops(ResourceLocation param0, Consumer<ItemStack> param1) {
        LootContext.DynamicDrop var0 = this.dynamicDrops.get(param0);
        if (var0 != null) {
            var0.add(this, param1);
        }

    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> param0) {
        return (T)this.params.get(param0);
    }

    public boolean hasVisitedElement(LootContext.VisitedEntry<?> param0) {
        return this.visitedElements.contains(param0);
    }

    public boolean pushVisitedElement(LootContext.VisitedEntry<?> param0) {
        return this.visitedElements.add(param0);
    }

    public void popVisitedElement(LootContext.VisitedEntry<?> param0) {
        this.visitedElements.remove(param0);
    }

    public LootDataResolver getResolver() {
        return this.lootDataResolver;
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.luck;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public static LootContext.VisitedEntry<LootTable> createVisitedEntry(LootTable param0) {
        return new LootContext.VisitedEntry<>(LootDataType.TABLE, param0);
    }

    public static LootContext.VisitedEntry<LootItemCondition> createVisitedEntry(LootItemCondition param0) {
        return new LootContext.VisitedEntry<>(LootDataType.PREDICATE, param0);
    }

    public static LootContext.VisitedEntry<LootItemFunction> createVisitedEntry(LootItemFunction param0) {
        return new LootContext.VisitedEntry<>(LootDataType.MODIFIER, param0);
    }

    public static class Builder {
        private final ServerLevel level;
        private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
        private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops = Maps.newHashMap();
        @Nullable
        private RandomSource random;
        private float luck;

        public Builder(ServerLevel param0) {
            this.level = param0;
        }

        public LootContext.Builder withRandom(RandomSource param0) {
            this.random = param0;
            return this;
        }

        public LootContext.Builder withOptionalRandomSeed(long param0) {
            if (param0 != 0L) {
                this.random = RandomSource.create(param0);
            }

            return this;
        }

        public LootContext.Builder withOptionalRandomSeed(long param0, RandomSource param1) {
            if (param0 == 0L) {
                this.random = param1;
            } else {
                this.random = RandomSource.create(param0);
            }

            return this;
        }

        public LootContext.Builder withLuck(float param0) {
            this.luck = param0;
            return this;
        }

        public <T> LootContext.Builder withParameter(LootContextParam<T> param0, T param1) {
            this.params.put(param0, param1);
            return this;
        }

        public <T> LootContext.Builder withOptionalParameter(LootContextParam<T> param0, @Nullable T param1) {
            if (param1 == null) {
                this.params.remove(param0);
            } else {
                this.params.put(param0, param1);
            }

            return this;
        }

        public LootContext.Builder withDynamicDrop(ResourceLocation param0, LootContext.DynamicDrop param1) {
            LootContext.DynamicDrop var0 = this.dynamicDrops.put(param0, param1);
            if (var0 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
            } else {
                return this;
            }
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> T getParameter(LootContextParam<T> param0) {
            T var0 = (T)this.params.get(param0);
            if (var0 == null) {
                throw new IllegalArgumentException("No parameter " + param0);
            } else {
                return var0;
            }
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParam<T> param0) {
            return (T)this.params.get(param0);
        }

        public LootContext create(LootContextParamSet param0) {
            Set<LootContextParam<?>> var0 = Sets.difference(this.params.keySet(), param0.getAllowed());
            if (!var0.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + var0);
            } else {
                Set<LootContextParam<?>> var1 = Sets.difference(param0.getRequired(), this.params.keySet());
                if (!var1.isEmpty()) {
                    throw new IllegalArgumentException("Missing required parameters: " + var1);
                } else {
                    RandomSource var2 = this.random;
                    if (var2 == null) {
                        var2 = RandomSource.create();
                    }

                    MinecraftServer var3 = this.level.getServer();
                    return new LootContext(var2, this.luck, this.level, var3.getLootData(), this.params, this.dynamicDrops);
                }
            }
        }
    }

    @FunctionalInterface
    public interface DynamicDrop {
        void add(LootContext var1, Consumer<ItemStack> var2);
    }

    public static enum EntityTarget {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

        final String name;
        private final LootContextParam<? extends Entity> param;

        private EntityTarget(String param0, LootContextParam<? extends Entity> param1) {
            this.name = param0;
            this.param = param1;
        }

        public LootContextParam<? extends Entity> getParam() {
            return this.param;
        }

        public static LootContext.EntityTarget getByName(String param0) {
            for(LootContext.EntityTarget var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid entity target " + param0);
        }

        public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
            public void write(JsonWriter param0, LootContext.EntityTarget param1) throws IOException {
                param0.value(param1.name);
            }

            public LootContext.EntityTarget read(JsonReader param0) throws IOException {
                return LootContext.EntityTarget.getByName(param0.nextString());
            }
        }
    }

    public static record VisitedEntry<T>(LootDataType<T> type, T value) {
    }
}
